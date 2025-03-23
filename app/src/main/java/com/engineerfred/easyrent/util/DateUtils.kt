package com.engineerfred.easyrent.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val calendar: Calendar = Calendar.getInstance()

    fun isDateWithinRange(): Boolean {
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return dayOfMonth in 25..lastDayOfMonth
    }

    fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(this))
    }

    fun getCurrentMonthAndYear(): String {
        val currentYear = calendar.get(Calendar.YEAR)
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        return "$monthName $currentYear"
    }

}