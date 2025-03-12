package com.engineerfred.easyrent.presentation.screens.expenses

import com.engineerfred.easyrent.domain.modals.Expense

data class ExpensesUiState(
    val isFetching: Boolean = true,
    val fetchErr: String? = null,
    val isDeleting: Boolean = false,
    val deleteErr: String? = null,
    val success: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val title: String = "",
    val notes: String? = null,
    val amount: String = "",
    val category: String? = null,
    val totalExpenses: String = "",
    val isInserting: Boolean = false,
    val insertionErr: String? = null,
    val insertSuccess: Boolean = false
)  {
    val isValidForm = title.isNotEmpty() && amount.isNotEmpty() && amount.toFloatOrNull() != null && category.isNullOrEmpty().not()
}
