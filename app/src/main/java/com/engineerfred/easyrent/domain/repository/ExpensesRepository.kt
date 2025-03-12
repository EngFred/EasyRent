package com.engineerfred.easyrent.domain.repository

import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Expense
import kotlinx.coroutines.flow.Flow

interface ExpensesRepository {
    suspend fun insertExpense(expense: Expense) : Resource<Unit>
    suspend fun cacheAllRemoteExpenses()
    suspend fun deleteExpense(expense: Expense) : Resource<Unit>
    fun getAllExpenses(): Flow<Resource<List<Expense>>>
    suspend fun getAllTrashedExpenses(): List<Expense>
    suspend fun getAllUnsyncedExpenses(): List<Expense>
}