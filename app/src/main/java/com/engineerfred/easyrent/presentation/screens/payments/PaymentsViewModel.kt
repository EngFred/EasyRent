package com.engineerfred.easyrent.presentation.screens.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.usecases.payments.DeletePaymentsUseCase
import com.engineerfred.easyrent.domain.usecases.payments.GetAllPaymentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val getAllPaymentsUseCase: GetAllPaymentsUseCase,
    private val deletePaymentsUseCase: DeletePaymentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchPayments()
    }

    fun onEvent(event: PaymentsUiEvents) {
        when(event) {
            is PaymentsUiEvents.PaymentDeleted -> {
                deletePayment(event.payment)
            }
        }
    }

    private fun deletePayment(payment: Payment) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update {
            it.copy(
                deletingPayment = true,
                deletedPaymentId = payment.id
            )
        }
        val result = deletePaymentsUseCase.invoke(payment)
        when(result){
            is Resource.Error -> {
                _uiState.update {
                    it.copy(
                        deletingPayment = false,
                        deletingPaymentErr = result.msg
                    )
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        deletingPayment = false,
                        deletingPaymentErr = null
                    )
                }
            }
        }

    }

    private fun fetchPayments() =  viewModelScope.launch {
        getAllPaymentsUseCase.invoke().collect{ result ->
            when(result) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fetchErr = result.msg
                        )
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            payments = result.data
                        )
                    }
                }
            }
        }
    }

}