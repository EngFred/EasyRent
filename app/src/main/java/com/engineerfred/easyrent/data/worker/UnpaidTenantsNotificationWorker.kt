package com.engineerfred.easyrent.data.worker

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.engineerfred.easyrent.util.ChannelNames
import com.engineerfred.easyrent.util.DateUtils
import com.engineerfred.easyrent.util.NotificationDismissReceiver
import com.engineerfred.easyrent.util.formatCurrency
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
        try {

            Log.wtf("MyWorker", "UnpaidTenantsWorker started!")
            val userId = prefs.getUserId().firstOrNull()

            if ( userId == null ) {
                Log.e(TAG, "User ID is null!")
                return Result.failure()
            }

            if( !DateUtils.isDateWithinRange() ) {
                Log.d(TAG, "It's not yet time!")
                return  Result.success()
            }

            val unpaidTenants = cache.tenantsDao().getUnpaidTenants(userId)

            if (unpaidTenants.isNotEmpty()) {
                Log.d(TAG, "Found ${unpaidTenants.size} tenants with balance!")
                setForeground(createForegroundInfo(unpaidTenants))
            }

            Log.d(TAG, "Unpaid tenants notification sent successfully!")
            return Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending unpaid tenants notification: ${ex.message}")
            return Result.failure()
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

        val tenantDetails = unpaidTenants.joinToString("\n") {
            "• ${it.name.replaceFirstChar { char -> char.uppercase() }} - UGX.${formatCurrency(it.balance)}"
        }

        val tenantsCount = unpaidTenants.size

        val notificationText = when {
            tenantsCount == 1 -> {
                "One tenant has an outstanding balance! Tap to view details."
            }
            else -> {
                "$tenantsCount tenants have outstanding balances! Tap to view details."
            }
        }

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ChannelNames.UnpaidTenantsChannel.name)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Pending Rent Payments:\n$tenantDetails"))
            .setSmallIcon(R.drawable.easy_rent_app_logo)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_close, "Seen", dismissPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return notification
    }
}
