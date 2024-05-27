package com.example.todoapp.alarm

interface AlarmScheduler {
    fun scheduleAlarm(taskId: Int, dueTime: Long, title: String, content: String)
    fun cancelAlarm(taskId: Int)
}