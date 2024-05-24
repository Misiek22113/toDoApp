package com.example.todoapp.view

import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.Task

data class TaskState(
    val tasks: List<Task> = emptyList(),
    val title: String = "",
    val description: String = "",
    val createTime: Long = 0,
    val dueTime: Long = 0,
    val notifications: Boolean = false,
    val isCompleted: Boolean = false,
    val category: String = "",
    val attachments: List<String> = emptyList(),
    val isAddingTask: Boolean = false,
    val filter: String = "",
    val isFiltered: CategoryType = CategoryType.NONE,
    val isDoneTaskFiler: Boolean = false,
    val query: String = ""
)
