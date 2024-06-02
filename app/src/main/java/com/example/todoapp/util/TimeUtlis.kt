package com.example.todoapp.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun convertToUtcZero(scheduledTime: Long, context: Context): Long {
        val sharedPref = SharedPreferences(context)
        val notificationTime = sharedPref.loadNotificationTime()
        val offset = TimeUnit.MINUTES.toMillis(notificationTime.toLong())

        val timeZone: TimeZone = TimeZone.getDefault()
        val timeZoneOffset: Int = timeZone.getOffset(Calendar.getInstance().getTimeInMillis())

        val result = scheduledTime - offset - timeZoneOffset

        return result
    }


    fun getCurrentTime(): Long {
        val timeZone: TimeZone = TimeZone.getDefault()
        val timeZoneOffset: Int = timeZone.getOffset(Calendar.getInstance().getTimeInMillis())

        return System.currentTimeMillis() + timeZoneOffset
    }

    fun getFinalTime(dueDate: Long, hours: Int, minutes: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueDate
            add(Calendar.HOUR_OF_DAY, hours)
            add(Calendar.MINUTE, minutes)
        }
        return calendar.timeInMillis
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun convertToHourMinute(dueTime: Long): Any {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dueTime

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return Pair(hour, minute)
    }
}