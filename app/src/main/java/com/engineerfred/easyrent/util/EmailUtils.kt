package com.engineerfred.easyrent.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri

object EmailUtils {
    fun sendEmail(context: Context, recipient: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$recipient".toUri() // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Choose Email Client..."))
            }
        } catch (e: Exception) {
           Log.e("EmailUtils", "Error sending email ${e.message}")
        }
    }
}