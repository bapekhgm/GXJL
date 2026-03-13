package com.example.processrecord.ui.screen

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_DAY = 86_400_000L

data class DurationParts(
    val days: Long,
    val hours: Long,
    val minutes: Long
)

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun calculateDurationParts(startTime: Long, endTime: Long): DurationParts? {
    if (startTime <= 0L || endTime <= startTime) return null

    val durationMillis = endTime - startTime
    val days = durationMillis / MILLIS_PER_DAY
    val hours = (durationMillis % MILLIS_PER_DAY) / MILLIS_PER_HOUR
    val minutes = (durationMillis % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
    return DurationParts(days = days, hours = hours, minutes = minutes)
}
