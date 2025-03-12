package com.engineerfred.easyrent.data.worker

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.data.local.db.CacheDatabase
import com.engineerfred.easyrent.data.local.entity.TenantEntity
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class UnpaidTenantsWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val cache: CacheDatabase,
    @Assisted val prefs: PreferencesRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "UnpaidTenantsWorker"
        private const val NOTIFICATION_ID = 4
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Checking for tenants with balance...")
        return try {
            val userId = prefs.getUserId().firstOrNull()
            if (userId != null) {
                val unpaidTenants = cache.tenantsDao().getUnpaidTenants(userId)
                if (unpaidTenants.isNotEmpty()) {
                    Log.d(TAG, "Found ${unpaidTenants.size} tenants with balance!")
                    setForeground(createForegroundInfo(unpaidTenants))
                } else {
                    Log.d(TAG, "Found no tenants with balance!")
                }
                Result.success()
            } else {
                Result.failure()
            }
        } catch (ex: Exception) {
            Log.e(TAG, "${ex.message}")
            Result.retry()
        }
    }

    private fun createForegroundInfo(unpaidTenants: List<TenantEntity>): ForegroundInfo {

        val notification = sendNotification(unpaidTenants)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun sendNotification(unpaidTenants: List<TenantEntity>): Notification {

        val tenantNames = unpaidTenants.joinToString(", ") { it.name }

        val notification = NotificationCompat.Builder(context, "unpaid_tenants_channel")
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText("The following tenants still owe rent: $tenantNames")
            .setSmallIcon(R.drawable.k_logo)
            .setOngoing(false)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

}
