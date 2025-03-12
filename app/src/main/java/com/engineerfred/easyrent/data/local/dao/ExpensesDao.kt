package com.engineerfred.easyrent.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.engineerfred.easyrent.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpensesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheAllRemoteExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense( expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 AND userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isDeleted = 1 AND userId = :userId")
    suspend fun getAllTrashedExpenses(userId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0  AND isSynced = 0 AND userId = :userId")
    suspend fun getAllUnsyncedExpenses(userId: String): List<ExpenseEntity>
}