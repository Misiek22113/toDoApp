package com.example.todoapp

import android.content.Context
import com.example.todoapp.util.TaskNotificationService
import com.example.todoapp.alarm.TaskAlarmScheduler
import com.example.todoapp.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationManager(private val context: Context) {
    private val service = TaskNotificationService(context)
    private val taskAlarmManager = TaskAlarmScheduler(context)

    fun setNotification(title: String, description: String, dueTime: Long, taskId: Int) {
        val triggerTime = TimeUtils.convertToUtcZero(dueTime, context)
        taskAlarmManager.scheduleAlarm(taskId, triggerTime, title, description)
    }
}