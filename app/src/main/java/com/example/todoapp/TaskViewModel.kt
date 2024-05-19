package com.example.todoapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class TaskViewModel(
    private val dao: TaskDao
): ViewModel() {

    private val _filterType = MutableStateFlow(SortType.BY_DATE)
    private val _state = MutableStateFlow(TaskState())
}