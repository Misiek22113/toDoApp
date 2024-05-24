package com.example.todoapp.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    val title: String,
    val description: String,
    val createTime: Long,
    val dueTime: Long,
    val notifications: Boolean,
    val isCompleted: Boolean,
    val category: String,
    val attachments: List<String>,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
)
