package com.example.todoapp.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R

class TaskNotificationService(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showNotification(title: String, content: String, taskId: Int) {
        val taskActivityIntent = Intent(context, MainActivity::class.java)
        val activityTaskPendingIntent = PendingIntent.getActivity(
            context,
            0,
            taskActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(activityTaskPendingIntent)
            .build()

        notificationManager.notify(taskId, notification)
    }

    companion object {
        const val CHANNEL_ID = "task_channel"
    }
}