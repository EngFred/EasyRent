package com.engineerfred.easyrent.presentation.screens.payments

import com.engineerfred.easyrent.domain.modals.Payment

data class PaymentsUiState(
    val isLoading: Boolean = true,
    val fetchErr: String? = null,
    val deletingPaymentErr: String? = null,
    val deletingPayment: Boolean = false,
    val deletedPaymentId: String? = null,
    val payments: List<Payment> = emptyList()
)
