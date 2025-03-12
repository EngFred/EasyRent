package com.engineerfred.easyrent.domain.usecases.tenants

import com.engineerfred.easyrent.domain.repository.TenantsRepository
import javax.inject.Inject

class GetTenantInRoomUseCase @Inject constructor(
    private val tenantsRepository: TenantsRepository
) {
    suspend operator fun invoke(roomId: String) = tenantsRepository.getTenantInRoom(roomId)
}