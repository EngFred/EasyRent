package com.engineerfred.easyrent.domain.usecases.tenants

import com.engineerfred.easyrent.domain.modals.Tenant
import com.engineerfred.easyrent.domain.repository.TenantsRepository
import javax.inject.Inject


class RemoveTenantUseCase @Inject constructor(
    private val tenantsRepository: TenantsRepository
){
    suspend operator fun invoke(tenant: Tenant) = tenantsRepository.deleteTenant(tenant)
}