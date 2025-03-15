package com.engineerfred.easyrent.domain.usecases.payments

import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import javax.inject.Inject

class GetUnsyncedPaymentsUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {

    suspend operator fun invoke() = paymentsRepository.getUnsyncedPayments()
}