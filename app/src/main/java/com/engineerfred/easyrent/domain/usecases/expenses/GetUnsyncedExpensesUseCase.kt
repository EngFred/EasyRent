package com.engineerfred.easyrent.domain.usecases.expenses

import com.engineerfred.easyrent.domain.repository.ExpensesRepository
import javax.inject.Inject

class GetUnsyncedExpensesUseCase @Inject constructor(
    private val expensesRepository: ExpensesRepository
) {
    suspend operator fun invoke() = expensesRepository.getUnsyncedExpenses()
}