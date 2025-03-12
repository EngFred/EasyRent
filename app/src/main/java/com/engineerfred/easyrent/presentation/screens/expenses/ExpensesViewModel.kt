package com.engineerfred.easyrent.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Expense
import com.engineerfred.easyrent.domain.usecases.expenses.DeleteExpenseUseCase
import com.engineerfred.easyrent.domain.usecases.expenses.GetAllUserExpensesUseCase
import com.engineerfred.easyrent.domain.usecases.expenses.InsertExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val insertExpenseUseCase: InsertExpenseUseCase,
    private val getAllUserExpensesUseCase: GetAllUserExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getUserExpenses()
    }

    fun onEvent(event: ExpensesUiEvents) {
        _uiState.update {
            it.copy(
                insertionErr = null, deleteErr = null, fetchErr = null
            )
        }
        when(event) {
            is ExpensesUiEvents.DeleteButtonClicked -> {
                _uiState.update {
                    it.copy(isDeleting = true)
                }
                deleteExpense(event.expense)
            }
            ExpensesUiEvents.SaveButtonClicked -> {
                val isValidAmount = _uiState.value.amount.toFloatOrNull() != null
                if ( isValidAmount && _uiState.value.title.isNotEmpty() && !_uiState.value.category.isNullOrEmpty() ) {
                    val expense = Expense(
                        userId = "",
                        title = _uiState.value.title,
                        amount = _uiState.value.amount.toFloat(),
                        category = _uiState.value.category!!,
                        notes = _uiState.value.notes
                    )
                    saveExpense(expense)
                }
            }

            is ExpensesUiEvents.ChangedCategory -> {
                _uiState.update {
                    it.copy(category = event.category)
                }
            }

            is ExpensesUiEvents.TitleChanged -> {
                _uiState.update {
                    it.copy(title = event.title)
                }
            }

            is ExpensesUiEvents.AmountChanged -> {
                _uiState.update {
                    it.copy(amount = event.amount)
                }
            }

            ExpensesUiEvents.OnRetry -> {
                getUserExpenses()
            }

            is ExpensesUiEvents.NotesChanged -> {
                _uiState.update {
                    it.copy(notes = event.notes)
                }
            }
        }
    }

    private fun getUserExpenses() = viewModelScope.launch {
        getAllUserExpensesUseCase().collect{ result ->
            when(result){
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isFetching = false, fetchErr = result.msg)
                    }
                }
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isFetching = false, expenses = result.data)
                    }
                }
            }
        }
    }

    private fun saveExpense(expense: Expense) = viewModelScope.launch( Dispatchers.IO ) {
        val result = insertExpenseUseCase(expense)
        when(result){
            is Resource.Error -> {
                _uiState.update {
                    it.copy(isInserting = false, insertionErr = result.msg)
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        isInserting =  false,
                        insertSuccess = true,
                        title = "",
                        amount = "",
                        category = null,
                        notes = null
                    )
                }
            }
        }
    }

    private fun deleteExpense(expense: Expense) = viewModelScope.launch( Dispatchers.IO ) {
        val result = deleteExpenseUseCase.invoke(expense)
        when(result){
            is Resource.Error -> {
                _uiState.update {
                    it.copy(isDeleting = false, deleteErr = result.msg)
                }
            }
            Resource.Loading -> Unit
            is Resource.Success -> {
                _uiState.update {
                    it.copy(isDeleting =  false)
                }
            }
        }
    }

}