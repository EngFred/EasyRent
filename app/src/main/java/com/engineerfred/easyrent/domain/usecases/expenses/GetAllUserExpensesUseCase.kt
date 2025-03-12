package com.engineerfred.easyrent.domain.usecases.expenses

import com.engineerfred.easyrent.domain.repository.ExpensesRepository
import javax.inject.Inject

class GetAllUserExpensesUseCase @Inject constructor(
    private val expensesRepository: ExpensesRepository
) {
    operator fun invoke() = expensesRepository.getAllExpenses()
}