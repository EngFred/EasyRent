package com.engineerfred.easyrent.presentation.screens.add_payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.usecases.payments.InsertPaymentsUseCase
import com.engineerfred.easyrent.domain.usecases.rooms.GetRoomByIdUseCase
import com.engineerfred.easyrent.domain.usecases.tenants.GetAllTenantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPaymentViewModel @Inject constructor(
    private val insertPaymentsUseCase: InsertPaymentsUseCase,
    private val getAllTenantsUseCase: GetAllTenantsUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPaymentUIState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchTenants()
    }


    fun onEvent(event: AddPaymentUiEvents) {
        if ( _uiState.value.insertionErr != null ) {
            _uiState.update {
                it.copy(
                    insertionErr = null
                )
            }
        }
        when(event) {
            is AddPaymentUiEvents.AmountChanged -> {
                _uiState.update {
                    it.copy(
                        amount = event.amount
                    )
                }
                calculateBalance(event.amount)
            }
            is AddPaymentUiEvents.PaymentMethodChanged -> {
                _uiState.update {
                    it.copy(
                        paymentMethod = event.paymentMethod
                    )
                }
            }
            AddPaymentUiEvents.SaveClicked -> {

                if(
                    _uiState.value.selectedTenant != null &&
                    _uiState.value.room != null &&
                    _uiState.value.amount != null &&
                    _uiState.value.amount!!.toFloatOrNull() != null &&
                    _uiState.value.balanceCalcErr == null &&
                    _uiState.value.balance != null
                    ) {
                    _uiState.update {
                        it.copy(
                            isInserting = true
                        )
                    }
                    val payment = Payment(
                        amount =_uiState.value.amount!!.toFloatOrNull()!!,
                        newBalance = _uiState.value.balance!!,
                        tenantId = _uiState.value.selectedTenant!!.id,
                        tenantName = _uiState.value.selectedTenant!!.name,
                        roomNumber = _uiState.value.room!!.roomNumber,
                        paymentMethod = _uiState.value.paymentMethod,
                        userId = ""
                    )

                    savePayment(payment)
                }
            }
            is AddPaymentUiEvents.SelectedTenant -> {
                fetchTenantRoom(event.tenant.roomId)
                _uiState.update {
                    it.copy(
                        selectedTenant = event.tenant,
                        balance = 0F,
                        balanceCalcErr = null
                    )
                }
                calculateBalance(_uiState.value.amount ?: "")
            }
        }
    }

    private fun calculateBalance(amountString: String) {
        _uiState.update {
            it.copy(
                balanceCalcErr = null
            )
        }
        val amount = amountString.toFloatOrNull()
        if ( _uiState.value.selectedTenant != null && _uiState.value.room != null ) {
            if (amount != null) {
                if (_uiState.value.room != null) {
                    _uiState.update {
                        it.copy(
                            balance = when {
                                (_uiState.value.selectedTenant!!.balance == _uiState.value.room!!.monthlyRent) -> _uiState.value.room?.monthlyRent!! - amount
                                else -> _uiState.value.selectedTenant?.balance!! - amount
                            }
                        )
                    }

                    if (_uiState.value.balance != null && _uiState.value.balance!! < 0F) {
                        _uiState.update {
                            it.copy(
                                balanceCalcErr = "Can't pay that!"
                            )
                        }
                    }
                }
            }
        }

    }

    private fun savePayment(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        val result  = insertPaymentsUseCase.invoke(payment)
        when(result) {
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        isInserting = false,
                        insertionErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        insertionSuccess = true
                    )
                }
            }
        }
    }

    private fun fetchTenantRoom(roomId: String) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                fetchingTenantRoomInfo = true
            )
        }
        getRoomByIdUseCase.invoke(roomId).collect{ result ->
            when(result) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            fetchingTenantRoomInfo = false,
                            roomFetchErr = result.msg
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            fetchingTenantRoomInfo = false,
                            room = result.data
                        )
                    }
                }
            }
        }
    }

    private fun fetchTenants()  = viewModelScope.launch {
        _uiState.update {
            it.copy(
                fetchingTenants = true
            )
        }
        getAllTenantsUseCase.invoke().collect{ result ->
            when(result){
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            tenantsFetchErr = result.msg,
                            fetchingTenants = false
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            tenants = result.data,
                            fetchingTenants = false
                        )
                    }
                }
            }
        }
    }
}