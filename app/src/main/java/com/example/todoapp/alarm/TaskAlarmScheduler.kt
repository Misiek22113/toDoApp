package com.example.todoapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class TaskAlarmScheduler(
    private val context: Context
): AlarmScheduler {

    private val notificationManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleAlarm(taskId: Int, dueTime: Long, title: String, content: String) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("content", content)
            putExtra("taskId", taskId)
        }

        notificationManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dueTime,
            PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancelAlarm(taskId: Int) {
        notificationManager.cancel(
            PendingIntent.getBroadcast(
                context,
                taskId,
                Intent(context, TaskAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}