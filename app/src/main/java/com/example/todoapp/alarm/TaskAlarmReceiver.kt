package com.example.todoapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.util.TaskNotificationService

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = TaskNotificationService(context)

        val title = intent.getStringExtra("title") ?: return
        val content = intent.getStringExtra("content") ?: return
        val taskId = intent.getIntExtra("taskId", 0)

        service.showNotification(title, content, taskId)
    }
}

