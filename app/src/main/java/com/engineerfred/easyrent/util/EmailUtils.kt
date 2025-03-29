package com.engineerfred.easyrent.util

import android.content.Context
import android.content.Intent
import android.util.Log

object EmailUtils {
    fun sendMessage(context: Context, recipientEmail: String, recipientPhone: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // Allows sharing via multiple apps
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_PHONE_NUMBER, recipientPhone)
            }

            val chooser = Intent.createChooser(intent, "Choose Messaging App...")

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Log.e("EmailUtils", "Error sending message: ${e.message}")
        }
    }
}
