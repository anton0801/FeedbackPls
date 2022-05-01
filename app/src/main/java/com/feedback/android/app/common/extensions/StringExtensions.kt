package com.feedback.android.app.common.extensions

import com.feedback.android.app.common.Constants
import com.feedback.android.app.common.SharedManager
import java.text.SimpleDateFormat
import java.util.*

fun String.parseTimestampToBeautifulDate(): String {
    if (isNotBlank()) {
        val year = substring(0, 4).toInt()
        val month = substring(5, 7).toInt()
        val day = substring(8, 10).toInt()

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val dayToPrint = if (day <= 9) "0$day" else day
        val monthToPrint =
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

        return "$dayToPrint $monthToPrint $year"
    }
    return ""
}

fun String.checkIfNeedsToReLogin(sharedManager: SharedManager): Boolean {
    if (isBlank())
        return false
    if (!sharedManager.getBoolean(Constants.IS_AUTH_USER_KEY))
        return true
    val lastVisitedTime = Date(this.toLong())
    val currentTime = Date()
    val currentHour = SimpleDateFormat("hh", Locale.getDefault()).format(currentTime)
    return currentHour == "07" && (currentTime.time / 1000) - (lastVisitedTime.time / 1000) >= 3600
}