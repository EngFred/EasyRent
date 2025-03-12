package com.engineerfred.easyrent.domain.usecases.tenants

import com.engineerfred.easyrent.domain.repository.TenantsRepository
import javax.inject.Inject

class GetAllTenantsUseCase @Inject constructor(
    private val tenantsRepository: TenantsRepository
) {
    operator fun invoke() = tenantsRepository.getAllTenants()
}