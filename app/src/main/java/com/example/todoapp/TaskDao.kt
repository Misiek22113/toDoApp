package com.example.todoapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task_table WHERE isCompleted = :isCompleted")
    fun getTasks(isCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE category = :category")
    fun getTasksByCategory(category: String): Flow<List<Task>>
}