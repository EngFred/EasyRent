package com.engineerfred.easyrent.domain.usecases.expenses

import com.engineerfred.easyrent.domain.modals.Expense
import com.engineerfred.easyrent.domain.repository.ExpensesRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expensesRepository: ExpensesRepository
) {
    suspend operator fun invoke(expense: Expense ) = expensesRepository.deleteExpense(expense)
}