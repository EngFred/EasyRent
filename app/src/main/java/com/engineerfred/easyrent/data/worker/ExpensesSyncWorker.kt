package com.engineerfred.easyrent.data.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.constants.Constants.EXPENSES
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.ExpenseEntity
import com.engineerfred.easyrent.data.mappers.toExpenseDTO
import com.engineerfred.easyrent.data.mappers.toExpenseEntity
import com.engineerfred.easyrent.data.remote.dto.ExpenseDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
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
        setForeground(createForeGroundInfo())
        Log.wtf("MyWorker", "ExpensesSyncWorker started!")

        val userId = prefs.getUserId().firstOrNull()
        if ( userId != null ) {

            val locallyDeletedExpenses = cache.expenseDao().getAllTrashedExpenses(userId)
            if ( locallyDeletedExpenses.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedExpenses.size} locally deleted expenses in cache!!")
                deleteExpensesFromSupabaseAndUpdateCache(locallyDeletedExpenses, userId)
            } else {
                Log.i(TAG, "Found no locally deleted expenses in cache!!")
            }

            val unsyncedExpenses = cache.expenseDao().getAllUnsyncedExpenses(userId)
            if ( unsyncedExpenses.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedExpenses.size} unsynced expenses in cache!!")
                addExpensesInSupabase(unsyncedExpenses)
            } else {
                Log.i(TAG, "Found no unsynced expenses in cache!!")
            }

            //removeNotification()
            return Result.success()
        } else {
            Log.i(TAG, "User Id is null")
            //removeNotification()
            return Result.failure()
        }
    }

    private suspend fun deleteExpensesFromSupabaseAndUpdateCache(locallyDeletedExpenses: List<ExpenseEntity>, userId: String){
        try {
            Log.i(TAG, "Deleting expenses from supabase...")
            locallyDeletedExpenses.forEach {
                val deletedExpense = client.from(EXPENSES).delete{
                    select()
                    filter {
                        eq("id", it.id)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<ExpenseDto>()
                deletedExpense?.let {  cache.expenseDao().deleteExpense(deletedExpense.toExpenseEntity())  }
            }
            Log.i(TAG, "Expenses deleted successfully!")
        } catch (ex: Exception) {
            Log.e(TAG, "Error syncing expenses (deleting): $ex")
        }
    }

    private suspend fun addExpensesInSupabase(unsyncedExpenses: List<ExpenseEntity>){
        try {
            Log.d(TAG, "Adding unsynced expenses to supabase...")
            client.from(EXPENSES).upsert(unsyncedExpenses.map { it.copy(isSynced = true).toExpenseDTO() }){
                select()
            }.decodeList<ExpenseDto>()
            Log.d(TAG, "Expenses added successfully to supabase! Updating expenses in cache...")
            cache.expenseDao().cacheAllRemoteExpenses(unsyncedExpenses.map { it.copy(isSynced = true) })
            Log.i(TAG, "Expenses updated successfully in cache as well! Sync complete.")
        } catch (ex: Exception) {
            Log.d(TAG, "Error syncing expenses (insertion): $ex")
        }
    }

    private fun createForeGroundInfo() : ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "expenses_channel")
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setTicker("Expenses")
            .setContentText("Syncing expenses...")
            .setSmallIcon(R.drawable.k_logo)
            .setOngoing(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

//    private fun removeNotification() {
//        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(NOTIFICATION_ID)
//    }

}