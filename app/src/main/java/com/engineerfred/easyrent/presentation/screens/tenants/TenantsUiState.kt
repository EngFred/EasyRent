package com.engineerfred.easyrent.presentation.screens.tenants

import com.engineerfred.easyrent.domain.modals.Tenant

data class TenantsUiState(
    val isLoading: Boolean = true,
    val fetchError: String? = null,
    val tenants: List<Tenant> = emptyList(),
    val deletingTenant: Boolean = false,
    val deletingTenantErr: String? = null,
    val deleteSuccessful: Boolean = false,

    val unSyncedTenants: List<Tenant> = emptyList(),
    val showSyncButton: Boolean = false,
    val showSyncRequired: Boolean = false
)
