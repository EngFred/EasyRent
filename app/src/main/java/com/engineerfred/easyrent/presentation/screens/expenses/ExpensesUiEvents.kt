package com.engineerfred.easyrent.presentation.screens.expenses

import com.engineerfred.easyrent.domain.modals.Expense

sealed class ExpensesUiEvents {
    data object SaveButtonClicked: ExpensesUiEvents()
    data object OnRetry: ExpensesUiEvents()
    data class DeletedExpense(val expense: Expense ): ExpensesUiEvents()
    data class ChangedCategory( val category: String ): ExpensesUiEvents()
    data class TitleChanged( val title: String ): ExpensesUiEvents()
    data class NotesChanged( val notes: String ): ExpensesUiEvents()
    data class AmountChanged( val amount: String ): ExpensesUiEvents()
}