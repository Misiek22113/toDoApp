package com.example.todoapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val dao: TaskDao
) : ViewModel() {

    private val _filterCategoryType = MutableStateFlow(CategoryType.NONE)
    private val _filterDoneTask = MutableStateFlow(false)
    private val _state = MutableStateFlow(TaskState())
    private val _tasks = combine(_filterDoneTask, _filterCategoryType) { isFiltered, filterType ->
        when (filterType) {
            CategoryType.NONE -> dao.getTasks(isFiltered)
            else -> dao.getTasksByCategory(filterType.name)
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    val state = combine(
        _state,
        _tasks,
        _filterDoneTask,
        _filterCategoryType
    ) { state, tasks, isFiltered, filterType ->
        state.copy(tasks = tasks, isDoneTaskFiler = isFiltered, filter = filterType.name)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskState())

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.DeleteTask -> {
                viewModelScope.launch {
                    dao.deleteTask(event.task)
                }
            }

            TaskEvent.AddTask -> {
                val title = _state.value.title
                val description = _state.value.description
                val createTime = _state.value.createTime
                val dueTime = _state.value.dueTime
                val notifications = _state.value.notifications
                val isCompleted = _state.value.isCompleted
                val category = _state.value.category
                val attachments = _state.value.attachments

                if (title.isBlank() || description.isBlank() ||
                    createTime == 0L || dueTime == 0L ||
                    category.isBlank()) {
                    return
                }

                val task = Task(
                    title,
                    description,
                    createTime,
                    dueTime,
                    notifications,
                    isCompleted,
                    category,
                    attachments
                )

                Log.i("Logcat", "Adding task: $task")

                viewModelScope.launch {
                    dao.upsertTask(task)
                }

                _state.update { it.copy(
                    title = "",
                    description = "",
                    createTime = 0,
                    dueTime = 0,
                    notifications = false,
                    isCompleted = false,
                    category = "",
                    attachments = emptyList()
                ) }
            }

            is TaskEvent.FilterDoneTasks -> {
                _filterDoneTask.value = event.isFiltered
            }

            is TaskEvent.FilterTasks -> {
                _filterCategoryType.value = event.filter
            }

            is TaskEvent.SetAttachments -> {
                _state.update {
                    it.copy(attachments = event.attachments)
                }
            }

            is TaskEvent.SetCategory -> {
                _state.update {
                    it.copy(category = event.category.name)
                }
            }

            is TaskEvent.SetCreateTime -> {
                _state.update {
                    it.copy(createTime = event.createTime)
                }
            }

            is TaskEvent.SetDescription -> {
                _state.update {
                    it.copy(description = event.description)
                }
            }

            is TaskEvent.SetDueTime -> {
                _state.update {
                    it.copy(dueTime = event.dueTime)
                }
            }

            is TaskEvent.SetIsCompleted -> {
                _state.update {
                    it.copy(isCompleted = event.isCompleted)
                }
            }

            is TaskEvent.SetNotifications -> {
                _state.update {
                    it.copy(notifications = event.notifications)
                }
            }

            is TaskEvent.SetTitle -> {
                _state.update {
                    it.copy(title = event.title)
                }
                Log.i("Logcat", "Setting title: ${event.title} to ${_state.value.title}")
            }

            TaskEvent.ShowDialog -> {
                _state.update {
                    it.copy(isAddingTask = true)
                }
            }

            TaskEvent.HideDialog -> {
                _state.update {
                    it.copy(isAddingTask = false)
                }
            }

            TaskEvent.AddTask -> TODO()
            TaskEvent.HideDialog -> TODO()
            TaskEvent.ShowDialog -> TODO()
        }
    }
}