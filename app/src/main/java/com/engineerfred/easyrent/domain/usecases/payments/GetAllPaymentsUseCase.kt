package com.engineerfred.easyrent.domain.usecases.payments

import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import javax.inject.Inject

class GetAllPaymentsUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {

    operator fun invoke() = paymentsRepository.getAllPayments()
}