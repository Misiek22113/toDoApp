package com.example.todoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.todoapp.util.TaskNotificationService

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TaskNotificationService.CHANNEL_ID,
            "Task Notification",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Task Notification Channel"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

    }


}