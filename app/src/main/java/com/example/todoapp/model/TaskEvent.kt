package com.example.todoapp.model

sealed interface TaskEvent {
    object AddTask : TaskEvent
    data class SetId(val id: Int) : TaskEvent
    data class SetTitle(val title: String) : TaskEvent
    data class SetDescription(val description: String) : TaskEvent
    data class SetCreateTime(val createTime: Long) : TaskEvent
    data class SetDueTime(val dueTime: Long) : TaskEvent
    data class SetNotifications(val notifications: Boolean) : TaskEvent
    data class SetIsCompleted(val isCompleted: Boolean) : TaskEvent
    data class SetCategory(val category: CategoryType) : TaskEvent
    data class SetAttachments(val attachments: List<String>) : TaskEvent
    object ShowDialog: TaskEvent
    object HideDialog: TaskEvent
    data class FilterDoneTasks(val isFiltered: Boolean): TaskEvent
    data class FilterTasks(val filter: CategoryType): TaskEvent
    data class DeleteTaskById(val taskId: Int): TaskEvent
    data class SearchTaskQuery(val query: String): TaskEvent
    data class UpdateTask(val task: Task): TaskEvent
}