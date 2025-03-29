package com.engineerfred.easyrent.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.constants.Constants.PAYMENTS
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.PaymentEntity
import com.engineerfred.easyrent.data.mappers.toPaymentDto
import com.engineerfred.easyrent.data.mappers.toPaymentEntity
import com.engineerfred.easyrent.data.remote.dto.PaymentDto
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
        Log.wtf("MyWorker", "PaymentsWorker started!")

        try {
            val userId = prefs.getUserId().firstOrNull()

            if (userId == null) {
                Log.e(TAG, "User ID is null. Cannot sync payments.")
                return Result.failure()
            }

            try {
                Log.i(TAG, "Setting foreground info for PaymentsSyncWorker...")
                setForeground(
                    createForeGroundInfo(
                        applicationContext,
                        NOTIFICATION_ID,
                        ChannelNames.PaymentsChannel.name,
                        "Syncing payments..."
                    )
                )
                Log.i(TAG, "Foreground info set successfully for PaymentsSyncWorker!")
            } catch (e: Exception){
                Log.e(TAG, "Error setting foreground info for PaymentsSyncWorker: ${e.message}")
            }

            val locallyDeletedPayments = cache.paymentsDao().getAllTrashedTenants(userId)
            if ( locallyDeletedPayments.isNotEmpty() ) {
                Log.i(TAG, "Found ${locallyDeletedPayments.size} locally deleted payments in cache!!")
                deletePaymentsFromSupabaseAndUpdateCache(locallyDeletedPayments, userId)
            }

            val unsyncedPayments = cache.paymentsDao().getAllUnsyncedPayments(userId)
            if ( unsyncedPayments.isNotEmpty() ) {
                Log.i(TAG, "Found ${unsyncedPayments.size} unsynced payments in cache!!")
                addPaymentsInSupabase(unsyncedPayments)
            }

            Log.i(TAG, "Payments synced successfully!")
            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.success()

        }catch (ex: Exception) {
            Log.e(TAG, "Error syncing payments: ${ex.message}")
            cancelNotification(applicationContext, NOTIFICATION_ID)
            return Result.failure()
        }
    }

    private suspend fun deletePaymentsFromSupabaseAndUpdateCache(locallyDeletedPayments: List<PaymentEntity>, userId: String){
        locallyDeletedPayments.forEachIndexed { index, payment ->
            Log.i(TAG, "Deleting payment ${index + 1} of ${locallyDeletedPayments.size}...")
            client.from(PAYMENTS).delete{
                filter {
                    eq("id", payment.id)
                    eq("user_id", userId)
                }
            }
            Log.i(TAG, "Deleted payment ${index + 1} of ${locallyDeletedPayments.size} from cloud! permanently deleting from cache...")
            cache.paymentsDao().deletePayment(payment)
            Log.i(TAG, "Deleted payment ${index + 1} of ${locallyDeletedPayments.size} from cache!")
        }
    }

    private suspend fun addPaymentsInSupabase(unsyncedPayments: List<PaymentEntity>){
        val remotePayments = client.from(PAYMENTS).upsert(unsyncedPayments.map { it.copy(isSynced = true).toPaymentDto() }){
            select()
        }.decodeList<PaymentDto>()
        cache.paymentsDao().insertPayments(remotePayments.map { it.toPaymentEntity() })
    }
}