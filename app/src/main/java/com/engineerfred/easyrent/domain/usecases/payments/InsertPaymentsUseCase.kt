package com.engineerfred.easyrent.domain.usecases.payments

import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.repository.PaymentsRepository
import javax.inject.Inject

class InsertPaymentsUseCase @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {

    suspend operator fun invoke(payment: Payment) = paymentsRepository.insertPayment(payment)
}