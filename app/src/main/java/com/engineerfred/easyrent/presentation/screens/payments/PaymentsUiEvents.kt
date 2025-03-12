package com.engineerfred.easyrent.presentation.screens.payments

import com.engineerfred.easyrent.domain.modals.Payment

sealed class PaymentsUiEvents {
    data class PaymentDeleted(val payment: Payment) : PaymentsUiEvents()
}