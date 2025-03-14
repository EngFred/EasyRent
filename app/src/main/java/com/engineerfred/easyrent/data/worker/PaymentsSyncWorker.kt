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
import com.engineerfred.easyrent.util.WorkerUtils.isRetryableError
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

            setForeground(
                createForeGroundInfo(
                    applicationContext,
                    NOTIFICATION_ID,
                    ChannelNames.PaymentsChannel.name,
                    "Payments",
                    "Syncing payments..."
                )
            )

            val locallyDeletedPayments = cache.paymentsDao().getAllTrashedTenants(userId)
            if ( locallyDeletedPayments.isNotEmpty() ) {
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
            return if (isRetryableError(ex)) {
                Result.retry()
            } else {
                cancelNotification(applicationContext, NOTIFICATION_ID)
                Result.failure()
            }
        }
    }

    private suspend fun deletePaymentsFromSupabaseAndUpdateCache(locallyDeletedPayments: List<PaymentEntity>, userId: String){
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
    }

    private suspend fun addPaymentsInSupabase(unsyncedPayments: List<PaymentEntity>){
        val remotePayments = client.from(PAYMENTS).upsert(unsyncedPayments.map { it.copy(isSynced = true).toPaymentDto() }){
            select()
        }.decodeList<PaymentDto>()
        cache.paymentsDao().insertPayments(remotePayments.map { it.toPaymentEntity() })
    }
}