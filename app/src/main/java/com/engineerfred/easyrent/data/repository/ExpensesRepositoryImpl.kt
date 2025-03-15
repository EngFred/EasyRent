package com.engineerfred.easyrent.data.repository

import android.util.Log
import com.engineerfred.easyrent.constants.Constants.EXPENSES
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.mappers.toExpense
import com.engineerfred.easyrent.data.mappers.toExpenseDTO
import com.engineerfred.easyrent.data.mappers.toExpenseEntity
import com.engineerfred.easyrent.data.remote.dto.ExpenseDto
import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Expense
import com.engineerfred.easyrent.domain.repository.ExpensesRepository
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpensesRepositoryImpl @Inject constructor(
    cacheDatabase: CacheDatabase,
    private val supabaseClient: SupabaseClient,
    private val prefsRepo: PreferencesRepository
) : ExpensesRepository {

    companion object {
        private const val TAG = "ExpensesRepositoryImpl"
    }

    private val expensesDao = cacheDatabase.expenseDao()

    override suspend fun insertExpense(expense: Expense): Resource<Unit> {
        try {
            val userId = prefsRepo.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User is null")
                return Resource.Error("User is not logged in")
            }

            Log.d(TAG, "Inserting expense in cache...")
            expensesDao.insertExpense(expense.copy(userId = userId).toExpenseEntity())

            try {
                Log.d(TAG, "Inserting expense in supabase...")
                val sbResult = supabaseClient.from(EXPENSES).insert(expense.copy( isSynced = true, userId = userId ).toExpenseDTO()){
                    select()
                }.decodeSingleOrNull<ExpenseDto>()

                sbResult?.let {
                    Log.d(TAG, "Updating cache...")
                    expensesDao.updateExpense(it.toExpenseEntity())
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Supabase insertion error: ${ex.message}")
            }

            Log.i(TAG, "Expense inserted successfully!")
            return Resource.Success(Unit)

        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }

    override suspend fun cacheAllRemoteExpenses() {
        try {
            val userId = prefsRepo.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User is null")
                return
            }

            val remoteExpenses = supabaseClient.from(EXPENSES).select{
                filter { eq("user_id", userId) }
            }.decodeList<ExpenseDto>()

            expensesDao.cacheAllRemoteExpenses(remoteExpenses.map { it.toExpenseEntity() })

        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
        }
    }

    override suspend fun deleteExpense(expense: Expense): Resource<Unit> {
        try {
            val userId = prefsRepo.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User is null")
                return Resource.Error("User is not logged in")
            }

            if ( userId != expense.userId ) {
                Log.e(TAG, "User id mismatch")
                return Resource.Error("Can't delete expense")
            }

            expensesDao.updateExpense(expense.copy(isDeleted = true, isSynced = false).toExpenseEntity())

            try {
                supabaseClient.from(EXPENSES).delete {
                    filter {
                        eq("id", expense.id)
                        eq("user_id", userId)
                    }
                }

                expensesDao.deleteExpense(expense.toExpenseEntity())
            }catch (ex: Exception) {
                Log.e(TAG, "Supabase expense deletion error: ${ex.message}")
            }

            return Resource.Success(Unit)
        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
            return Resource.Error("${ex.message}")
        }
    }

    override fun getAllExpenses(): Flow<Resource<List<Expense>>> = flow {
        val userID = prefsRepo.getUserId().firstOrNull()

        if ( userID == null ) {
            Log.e(TAG, "User is null")
            emit(Resource.Error("User is not logged in"))
            return@flow
        }

        val expensesFlow = expensesDao.getAllExpenses( userID ).map { cachedExpenses ->
            if ( cachedExpenses.isEmpty() ) {
                try {
                    val results = supabaseClient.from(EXPENSES).select{
                        filter { eq("user_id", userID) }
                    }.decodeList<ExpenseDto>()

                    if ( results.isNotEmpty() ) {
                        expensesDao.cacheAllRemoteExpenses(results.map { it.toExpenseEntity() })
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Supabase error: $ex")
                }
            }
            Resource.Success(cachedExpenses.map { it.toExpense() })
        }
        emitAll(expensesFlow)
    }.catch {
        emit(Resource.Error("${it.message}"))
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    override suspend fun getAllTrashedExpenses(): List<Expense> {
        try {
            val userID = prefsRepo.getUserId().firstOrNull()

            if ( userID == null ) {
                Log.e(TAG, "User is null")
                return emptyList()
            }

            val trashedExpenses = expensesDao.getAllTrashedExpenses(userID)
            return trashedExpenses.map { it.toExpense() }

        } catch (ex: Exception ){
            return emptyList()
        }
    }

    override suspend fun getUnsyncedExpenses(): List<Expense>? {
        try {
            val userId = prefsRepo.getUserId().firstOrNull() ?: return null

            val unsyncedExpenses = expensesDao.getUnsyncedExpenses(userId)
            return unsyncedExpenses.map { it.toExpense() }

        } catch (ex: Exception ){
            return null
        }
    }
}