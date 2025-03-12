package com.engineerfred.easyrent.domain.usecases.payments

import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import javax.inject.Inject

class GetPaymentsByTenantUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    operator fun invoke(tenantId: String) = paymentsRepository.getPaymentsByTenant(tenantId)
}