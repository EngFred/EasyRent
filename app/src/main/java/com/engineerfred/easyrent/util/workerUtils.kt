package com.engineerfred.easyrent.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.engineerfred.easyrent.R
import com.engineerfred.easyrent.data.worker.EndOfMonthBalanceSyncWorker
import com.engineerfred.easyrent.data.worker.ExpensesSyncWorker
import com.engineerfred.easyrent.data.worker.PaymentsSyncWorker
import com.engineerfred.easyrent.data.worker.RoomsSyncWorker
import com.engineerfred.easyrent.data.worker.TenantsSyncWorker
import com.engineerfred.easyrent.data.worker.UnpaidTenantsWorker
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object WorkerUtils {

    fun createForeGroundInfo(
        context: Context,
        notificationId: Int,
        channelId: String,
        notificationTitle: String,
        notificationText: String
    ) : ForegroundInfo {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.app_name))
            .setTicker(notificationTitle)
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.easy_rent_app_logo)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun isRetryableError(ex: Exception): Boolean {
        return ex is IOException || ex is TimeoutException
    }


    private fun scheduleEndOfMonthWorker(workerManager: WorkManager) {
        val calendar = Calendar.getInstance()

        // Move to the first day of next month
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Move back one day to get the last day of the current month
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        // Set execution time (e.g., 2 AM)
        calendar.set(Calendar.HOUR_OF_DAY, 2)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = OneTimeWorkRequestBuilder<EndOfMonthBalanceSyncWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        workerManager.enqueueUniqueWork(
            "EndOfMonthBalanceSyncWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSyncWorkers(workManager: WorkManager) {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val tenantsSyncRequest = PeriodicWorkRequestBuilder<TenantsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val roomsSyncRequest = PeriodicWorkRequestBuilder<RoomsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val paymentsSyncRequest = PeriodicWorkRequestBuilder<PaymentsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val expensesSyncRequest = PeriodicWorkRequestBuilder<ExpensesSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val unpaidTenantsRequest = PeriodicWorkRequestBuilder<UnpaidTenantsWorker>(24, TimeUnit.HOURS).build()

        workManager.enqueueUniquePeriodicWork(
            "TenantsSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            tenantsSyncRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "RoomsSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            roomsSyncRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "ExpensesSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            expensesSyncRequest
        )

        scheduleEndOfMonthWorker(workManager)

        workManager.enqueueUniquePeriodicWork(
            "UnpaidTenantsWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            unpaidTenantsRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "PaymentsSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            paymentsSyncRequest
        )
    }

    fun syncRoomsImmediately(workManager: WorkManager) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<RoomsSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork(
            "RoomsSyncNow",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    fun syncTenantsImmediately(workManager: WorkManager) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<TenantsSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork(
            "TenantsSyncNow",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    fun syncPaymentsImmediately(workManager: WorkManager) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<PaymentsSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork(
            "PaymentsSyncNow",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    fun syncExpensesImmediately(workManager: WorkManager) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<ExpensesSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork(
            "ExpensesSyncNow",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

}

sealed class ChannelNames(val name: String) {
    data object TenantsChannel : ChannelNames("tenants_channel")
    data object RoomsChannel : ChannelNames("rooms_channel")
    data object ExpensesChannel : ChannelNames("expenses_channel")
    data object UnpaidTenantsChannel : ChannelNames("unpaid_tenants_channel")
    data object PaymentsChannel : ChannelNames("payments_channel")
}

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra("notification_id", -1) ?: return
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}


