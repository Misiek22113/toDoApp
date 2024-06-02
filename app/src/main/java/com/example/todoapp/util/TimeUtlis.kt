package com.example.todoapp.util

import android.content.Context
import java.util.Calendar
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

    fun convertToHourMinute(dueTime: Long): Any {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dueTime

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return Pair(hour, minute)
    }
}