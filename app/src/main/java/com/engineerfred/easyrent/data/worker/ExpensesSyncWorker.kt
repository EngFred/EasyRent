package com.engineerfred.easyrent.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.EXPENSES
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.ExpenseEntity
import com.engineerfred.easyrent.data.mappers.toExpenseDTO
import com.engineerfred.easyrent.data.remote.dto.ExpenseDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.util.ChannelNames
import com.engineerfred.easyrent.util.WorkerUtils.cancelNotification
import com.engineerfred.easyrent.util.WorkerUtils.createForeGroundInfo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class ExpensesSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val client: SupabaseClient,
    @Assisted val cache: CacheDatabase,
    @Assisted val prefs: PreferencesRepository,
) : CoroutineWorker(
    context, workerParams
) {
    companion object {
        const val TAG = "ExpensesSyncWorker"
        private const val NOTIFICATION_ID = 8
    }

    override suspend fun doWork(): Result {
        Log.wtf("MyWorker", "ExpensesSyncWorker started!")

        try {
            val userId = prefs.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User ID is null. Cannot sync expenses.")
                return Result.failure()
            }

            try{
                Log.i(TAG, "Setting foreground info for ExpensesSyncWorker...")
                setForeground(
                    createForeGroundInfo(
                        applicationContext,
                        NOTIFICATION_ID,
                        ChannelNames.ExpensesChannel.name,
                        "Syncing expenses..."
                    )
                )
                Log.i(TAG, "Foreground info set successfully for ExpensesSyncWorker!")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting foreground info for ExpensesSyncWorker: ${e.message}")
            }

            val locallyDeletedExpenses = cache.expenseDao().getAllTrashedExpenses(userId)
            if ( locallyDeletedExpenses.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedExpenses.size} locally deleted expenses in cache!!")
                deleteExpensesFromSupabaseAndUpdateCache(locallyDeletedExpenses, userId)
            }

            val unsyncedExpenses = cache.expenseDao().getUnsyncedExpenses(userId)
            if ( unsyncedExpenses.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedExpenses.size} unsynced expenses in cache!!")
                addExpensesInSupabase(unsyncedExpenses)
            }

            Log.i(TAG, "Expenses synced successfully!")
            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.success()

        } catch (ex: Exception) {
            Log.e(TAG, "Error syncing expenses: ${ex.message}")
            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.failure()
        }
    }

    private suspend fun deleteExpensesFromSupabaseAndUpdateCache(locallyDeletedExpenses: List<ExpenseEntity>, userId: String){
        locallyDeletedExpenses.forEachIndexed { index, expense ->
            Log.i(TAG, "Deleting expense ${index + 1} of ${locallyDeletedExpenses.size}...")
            client.from(EXPENSES).delete{
                filter {
                    eq("id", expense.id)
                    eq("user_id", userId)
                }
            }

            Log.i(TAG, "Deleted expense ${index + 1} of ${locallyDeletedExpenses.size} from cloud! permanently deleting from cache...")
            cache.expenseDao().deleteExpense(expense)
            Log.i(TAG, "Deleted expense ${index + 1} of ${locallyDeletedExpenses.size} from cache!")
        }
    }

    private suspend fun addExpensesInSupabase(unsyncedExpenses: List<ExpenseEntity>){
        client.from(EXPENSES).upsert(unsyncedExpenses.map { it.copy(isSynced = true).toExpenseDTO() }){
            select()
        }.decodeList<ExpenseDto>()
        cache.expenseDao().cacheAllRemoteExpenses(unsyncedExpenses.map { it.copy(isSynced = true) })
    }
}