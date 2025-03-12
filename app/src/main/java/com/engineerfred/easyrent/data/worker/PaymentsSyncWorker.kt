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
import com.engineerfred.easyrent.constants.Constants.PAYMENTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.PaymentEntity
import com.engineerfred.easyrent.data.mappers.toPaymentDto
import com.engineerfred.easyrent.data.mappers.toPaymentEntity
import com.engineerfred.easyrent.data.remote.dto.PaymentDto
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class PaymentsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val cache: CacheDatabase,
    @Assisted val client: SupabaseClient,
    @Assisted val prefs: PreferencesRepository
) : CoroutineWorker(
    context, workerParams
) {
    companion object {
        const val TAG = "PaymentsSyncWorker"
        private const val NOTIFICATION_ID  = 3
    }

    override suspend fun doWork(): Result {
        setForeground(createForeGroundInfo())
        Log.wtf("MyWorker", "PaymentsWorker started!")

        val userId = prefs.getUserId().firstOrNull()
        if ( userId != null ) {

            val locallyDeletedPayments = cache.paymentsDao().getAllTrashedTenants(userId)
            if ( locallyDeletedPayments.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedPayments.size} locally deleted payments in cache!!")
                deletePaymentsFromSupabaseAndUpdateCache(locallyDeletedPayments, userId)
            } else {
                Log.i(TAG, "Found no locally deleted payments in cache!!")
            }

            val unsyncedPayments = cache.paymentsDao().getAllUnsyncedPayments(userId)
            if ( unsyncedPayments.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedPayments.size} unsynced payments in cache!!")
                addPaymentsInSupabase(unsyncedPayments)
            } else {
                Log.i(TAG, "Found no unsynced payments in cache!!")
            }

            //removeNotification()
            return Result.success()
        } else {
            Log.i(TAG, "User Id is null")
            //removeNotification()
            return Result.failure()
        }
    }

    private suspend fun deletePaymentsFromSupabaseAndUpdateCache(locallyDeletedPayments: List<PaymentEntity>, userId: String){
        try {
            Log.i(TAG, "Deleting rooms...")
            locallyDeletedPayments.forEach {
                val deletedPayment = client.from(PAYMENTS).delete{
                    select()
                    filter {
                        eq("id", it.id)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<PaymentDto>()

                deletedPayment?.let {
                    cache.paymentsDao().deletePayment(it.toPaymentEntity())
                }
            }
            Log.i(TAG, "Payments deleted successfully!")
        } catch (ex: Exception) {
            Log.e(TAG, "Error syncing payments (deleting): $ex")
        }
    }

    private suspend fun addPaymentsInSupabase(unsyncedPayments: List<PaymentEntity>){
        try {
            Log.d(TAG, "Adding unsynced payments to supabase...")
            val remotePayments = client.from(PAYMENTS).upsert(unsyncedPayments.map { it.copy(isSynced = true).toPaymentDto() }){
                select()
            }.decodeList<PaymentDto>()
            Log.d(TAG, "Payments added successfully to supabase! Updating payments in cache...")
            cache.paymentsDao().insertPayments(remotePayments.map { it.toPaymentEntity() })
            Log.i(TAG, "Payments updated successfully in cache as well! Sync complete.")
        } catch (ex: Exception) {
            Log.d(TAG, "Error syncing payments (insertion): $ex")
        }
    }

    private fun createForeGroundInfo() : ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "payments_channel")
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setTicker("Payments")
            .setContentText("Syncing payments...")
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