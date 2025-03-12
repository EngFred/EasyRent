package com.engineerfred.easyrent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Configuration
import com.engineerfred.easyrent.data.worker.WorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EasyRentApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: WorkerFactory

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val tenantsChannel = NotificationChannel(
            "tenants_channel",
            "Tenants",
            NotificationManager.IMPORTANCE_LOW
        )
        val unpaidTenantsChannel = NotificationChannel(
            "unpaid_tenants_channel",
            "Unpaid Tenants",
            NotificationManager.IMPORTANCE_HIGH
        )
        val roomsChannel = NotificationChannel(
            "rooms_channel",
            "Rooms",
            NotificationManager.IMPORTANCE_LOW
        )
        val paymentsChannel = NotificationChannel(
            "payments_channel",
            "Payments",
            NotificationManager.IMPORTANCE_LOW
        )

        val expensesChannel = NotificationChannel(
            "expenses_channel",
            "Expenses",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationChannels = listOf(tenantsChannel, roomsChannel, paymentsChannel, unpaidTenantsChannel, expensesChannel)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(notificationChannels)
    }

}