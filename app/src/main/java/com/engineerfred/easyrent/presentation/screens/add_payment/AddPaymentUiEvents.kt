package com.engineerfred.easyrent.presentation.screens.add_payment

import com.engineerfred.easyrent.domain.modals.Tenant

sealed class AddPaymentUiEvents {
    data object SaveClicked: AddPaymentUiEvents()
    data class AmountChanged(val amount: String) : AddPaymentUiEvents()
    data class PaymentMethodChanged(val paymentMethod: String) : AddPaymentUiEvents()
    data class SelectedTenant(val tenant: Tenant) : AddPaymentUiEvents()
}