package com.example.todoapp.util

import android.content.Context
import android.util.Log

class SharedPreferences(context: Context) {
    private val sharedPref = context.getSharedPreferences("YourSharedPrefs", Context.MODE_PRIVATE)

    fun saveNotificationTime(time: Int) {
        with(sharedPref.edit()) {
            putInt("NOTIFICATION_TIME", time)
            apply()
        }
        Log.i("SharedPreferences", "Notification time saved: $time")
    }

    fun loadNotificationTime(): Int {
        return sharedPref.getInt("NOTIFICATION_TIME", 0)
    }
}