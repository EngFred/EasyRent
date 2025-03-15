package com.engineerfred.easyrent.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.engineerfred.easyrent.constants.Constants.SUPABASE_URL
import com.engineerfred.easyrent.domain.modals.Payment
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Uri.compressAndConvertToByteArray(contentResolver: ContentResolver, quality: Int = 55): ByteArray {
    val inputStream = contentResolver.openInputStream(this) ?: return byteArrayOf()
    val bitmap = BitmapFactory.decodeStream(inputStream) ?: return byteArrayOf()

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}

fun verifyEmail(email: String) : String? {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    return when {
        email.isBlank() -> "Email cannot be empty"
        !email.matches(emailPattern) -> "Invalid email format"
        else -> null
    }
}

fun formatCurrency(figure: Float): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedFigure = formatter.format(figure)
    return formattedFigure ?: figure.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getMonthlyPaymentsTotal(payments: List<Payment>): String {
    val currentDate = LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue

    val monthlyPayments = payments.filter { payment ->
        val paymentDate = Instant.ofEpochMilli(payment.paymentDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        paymentDate.year == currentYear && paymentDate.monthValue == currentMonth
    }.sumOf { it.amount.toInt() }

    return formatCurrency(monthlyPayments.toFloat())
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentMonthAndYear() : String {
    val currentDate = LocalDate.now()
    return "${currentDate.month.name}, ${currentDate.year}"
}

fun buildImageUrl(imageFileName: String, bucketName: String) =
    "$SUPABASE_URL/storage/v1/object/${bucketName}/${imageFileName}"

