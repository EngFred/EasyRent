package com.engineerfred.easyrent.presentation.screens.add_payment

import com.engineerfred.easyrent.domain.modals.PaymentMethod
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant

data class AddPaymentUIState(
    val isInserting: Boolean = false,
    val fetchingTenants: Boolean = true,
    val fetchingTenantRoomInfo: Boolean = false,
    val insertionErr: String? = null,
    val tenantsFetchErr: String? = null,
    val roomFetchErr: String? = null,
    val insertionSuccess: Boolean = false,
    val tenants: List<Tenant> = emptyList(),
    val amount: String? = null,
    val balance: Float? = null,
    val balanceCalcErr: String? = null,
    val selectedTenant: Tenant? = null,
    val room: Room? = null,
    val paymentMethod: String = PaymentMethod.Cash.name
)
