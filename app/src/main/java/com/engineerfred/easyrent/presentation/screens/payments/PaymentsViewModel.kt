package com.engineerfred.easyrent.presentation.screens.payments

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.usecases.payments.DeletePaymentsUseCase
import com.engineerfred.easyrent.domain.usecases.payments.GetAllPaymentsUseCase
import com.engineerfred.easyrent.domain.usecases.payments.GetUnsyncedPaymentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val getAllPaymentsUseCase: GetAllPaymentsUseCase,
    private val deletePaymentsUseCase: DeletePaymentsUseCase,
    private val getUnsyncedPaymentsUseCase: GetUnsyncedPaymentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getUnsyncedPayments()
        fetchPayments()
    }

    fun onEvent(event: PaymentsUiEvents) {
        when(event) {
            is PaymentsUiEvents.PaymentDeleted -> {
                deletePayment(event.payment)
            }

            PaymentsUiEvents.PaymentFilterToggled -> {
                toggledPaymentFilter()
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

    private fun getUnsyncedPayments() = viewModelScope.launch( Dispatchers.IO ) {
        val result = getUnsyncedPaymentsUseCase.invoke()

        result?.let {

            _uiState.update {
                it.copy(
                    unSyncedPayments = result
                )
            }

            if ( result.isNotEmpty() ) {
                delay(2000)
                _uiState.update {
                    it.copy(
                        showSyncRequired = true,
                        showSyncButton = true
                    )
                }
                delay(5000)
                _uiState.update {
                    it.copy(
                        showSyncRequired = false
                    )
                }
                delay(5000)
                _uiState.update {
                    it.copy(
                        showSyncButton = false
                    )
                }
            }
        }
    }

    fun hideSyncButton(){
        _uiState.update {
            it.copy(
                showSyncButton = false,
                showSyncRequired = false
            )
        }
    }

    private fun toggledPaymentFilter() {
        _uiState.update { state ->
            state.copy(
                showCurrentMonthPaymentsOnly = !state.showCurrentMonthPaymentsOnly)
        }
        filterPayments()
    }

    @SuppressLint("NewApi")
    private fun filterPayments() {
        val allPayments = _uiState.value.payments
        if( allPayments.isNotEmpty() ) {
            val filteredPayments = if (_uiState.value.showCurrentMonthPaymentsOnly) {
                val currentMonth = LocalDate.now().monthValue
                val currentYear = LocalDate.now().year
                allPayments.filter {
                    val paymentDate = Instant.ofEpochMilli(it.paymentDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    val paymentMonth = paymentDate.monthValue
                    val paymentYear = paymentDate.year
                    paymentMonth == currentMonth && paymentYear == currentYear
                }
            } else {
                allPayments
            }
            _uiState.update { it.copy(payments = filteredPayments) }
        }
    }
}