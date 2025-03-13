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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    private val authenticatedUserId = supabaseClient.auth.currentUserOrNull()?.id
    private val expensesDao = cacheDatabase.expenseDao()

    override suspend fun insertExpense(expense: Expense): Resource<Unit> {
        return try {
            val userId = getUserId()
            if ( userId != null ) {
                Log.d(TAG, "Inserting expense in cache...")
                expensesDao.insertExpense(expense.copy(userId = userId).toExpenseEntity())
                Log.d(TAG, "Inserting expense in supabase...")
                val sbResult = supabaseClient.from(EXPENSES).insert(expense.copy( isSynced = true, userId = userId ).toExpenseDTO()){
                    select()
                }.decodeSingleOrNull<ExpenseDto>()
                sbResult?.let {
                    Log.d(TAG, "Updating cache...")
                    expensesDao.updateExpense(it.toExpenseEntity())
                }
                Log.i(TAG, "DONE INSERTING TENANT!!")
                Resource.Success(Unit)
            } else {
                Log.e(TAG, "CURRENT USER IS NOT AUTHENTICATED!!")
                Resource.Error("User is not authenticated!")
            }

        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    override suspend fun cacheAllRemoteExpenses() {
        try {
            val userId = getUserId()
            if ( userId != null ) {
                val remoteExpenses = supabaseClient.from(EXPENSES).select{
                    filter { eq("user_id", userId) }
                }.decodeList<ExpenseDto>()
                expensesDao.cacheAllRemoteExpenses(remoteExpenses.map { it.toExpenseEntity() })
                Resource.Success(Unit)
            } else {
                Resource.Error("User is not authenticated!")
            }

        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    override suspend fun deleteExpense(expense: Expense): Resource<Unit> {
        return try {
            val userId = getUserId()
            if ( userId != null ) {
                expensesDao.updateExpense(expense.copy(isDeleted = true, isSynced = false).toExpenseEntity())
                val result = supabaseClient.from(EXPENSES).delete {
                    select()
                    filter {
                        eq("id", expense.id)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<ExpenseDto>()
                result?.let { expensesDao.deleteExpense(expense.toExpenseEntity()) }
                Resource.Success(Unit)
            } else {
                Resource.Error("User is not authenticated!")
            }
        } catch (ex: Exception){
            Log.e(TAG, "${ex.message}")
            Resource.Error("${ex.message}")
        }
    }

    override fun getAllExpenses(): Flow<Resource<List<Expense>>> = flow {
        val userID = prefsRepo.getUserId().firstOrNull()
        if ( userID != null ) {
            val expensesFlow = expensesDao.getAllExpenses( userID ).map { cachedExpenses ->
                if ( cachedExpenses.isEmpty() ) {
                    val results = supabaseClient.from(EXPENSES).select{
                        filter { eq("user_id", userID) }
                    }.decodeList<ExpenseDto>()
                    if ( results.isNotEmpty() ) {
                        expensesDao.cacheAllRemoteExpenses(results.map { it.toExpenseEntity() })
                    }
                }
                Resource.Success(cachedExpenses.map { it.toExpense() })
            }
            emitAll(expensesFlow)
        } else {
            emit(Resource.Error("User is not authenticated"))
        }
    }.catch {
        emit(Resource.Error("${it.message}"))
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllTrashedExpenses(): List<Expense> {
        try {
            val userID = getUserId()
            if ( userID != null ) {
                val trashedExpenses = expensesDao.getAllTrashedExpenses(userID)
                return trashedExpenses.map { it.toExpense() }
            } else {
                return emptyList()
            }
        } catch (ex: Exception ){
            return emptyList()
        }
    }

    override suspend fun getAllUnsyncedExpenses(): List<Expense> {
        try {
            val userID = getUserId()
            if ( userID != null ) {
                val unsyncedExpenses = expensesDao.getAllUnsyncedExpenses(userID)
                return unsyncedExpenses.map { it.toExpense() }
            } else {
                return emptyList()
            }
        } catch (ex: Exception ){
            return emptyList()
        }
    }

    private suspend fun getUserId() : String? {
        val userIdInPrefs = prefsRepo.getUserId().firstOrNull()
        val userAuthenticated =  userIdInPrefs != null && userIdInPrefs == authenticatedUserId
        return  if ( userAuthenticated ) userIdInPrefs else null
    }
}