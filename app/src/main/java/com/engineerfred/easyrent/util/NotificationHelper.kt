package com.engineerfred.easyrent.util

//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.engineerfred.rentalmanagement.R
//
//class NotificationHelper(private val context: Context) {
//    fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "work_channel",
//                "Background Tasks",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Notifications for background tasks"
//            }
//            val manager = context.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    fun createNotification(): Notification {
//        return NotificationCompat.Builder(context, "work_channel")
//            .setContentTitle("Task in Progress")
//            .setContentText("Running background task...")
//            .setSmallIcon(R.drawable.ic_notification)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
//}
