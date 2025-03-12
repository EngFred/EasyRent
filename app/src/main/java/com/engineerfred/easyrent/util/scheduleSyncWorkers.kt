package com.engineerfred.easyrent.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.engineerfred.easyrent.data.worker.EndOfMonthBalanceSyncWorker
import com.engineerfred.easyrent.data.worker.ExpensesSyncWorker
import com.engineerfred.easyrent.data.worker.PaymentsSyncWorker
import com.engineerfred.easyrent.data.worker.RoomsSyncWorker
import com.engineerfred.easyrent.data.worker.TenantsSyncWorker
import com.engineerfred.easyrent.data.worker.UnpaidTenantsWorker
import java.util.concurrent.TimeUnit


object WorkerUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSyncWorkers(workManager: WorkManager) {

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val tenantsSyncRequest = PeriodicWorkRequestBuilder<TenantsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val roomsSyncRequest = PeriodicWorkRequestBuilder<RoomsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val paymentsSyncRequest = PeriodicWorkRequestBuilder<PaymentsSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        val expensesSyncRequest = PeriodicWorkRequestBuilder<ExpensesSyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()

        val endOfMonthWorkRequest = OneTimeWorkRequestBuilder<EndOfMonthBalanceSyncWorker>()
            .setInitialDelay(getTimeUntilEndOfMonth(), TimeUnit.MILLISECONDS)
            .build()

        val unpaidTenantsRequest = PeriodicWorkRequestBuilder<UnpaidTenantsWorker>(15, TimeUnit.MINUTES).build()

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

        workManager.enqueueUniqueWork(
            "EndOfMonthBalanceSyncWorker",
            ExistingWorkPolicy.REPLACE,
            endOfMonthWorkRequest
        )

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
}
