package com.engineerfred.easyrent.domain.usecases.tenants

import android.content.ContentResolver
import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.repository.TenantsRepository
import javax.inject.Inject

class AddTenantUseCase @Inject constructor(
    private val tenantsRepository: TenantsRepository
) {
    suspend operator fun invoke(tenant: Tenant, contentResolver: ContentResolver) = tenantsRepository.insertTenant(tenant, contentResolver)
}