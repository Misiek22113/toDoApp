package com.example.todoapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.todoapp.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertTask(task: Task)
    @Query("DELETE FROM task_table WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int);
    @Query("SELECT * FROM task_table WHERE (:isCompleted IS NULL OR isCompleted = :isCompleted) AND title LIKE '%' || :query || '%' ORDER BY dueTime ASC")
    fun getTasks(isCompleted: Boolean?, query: String): Flow<List<Task>>
    @Query("SELECT * FROM task_table WHERE category = :category AND  (:isCompleted IS NULL OR isCompleted = :isCompleted) AND title LIKE '%' || :query || '%' ORDER BY dueTime ASC")
    fun getTasksByCategory(category: String, query: String, isCompleted: Boolean?): Flow<List<Task>>
    @Query("SELECT * FROM task_table WHERE title LIKE '%' || :query || '%' ORDER BY dueTime ASC")
    fun searchTask(query: String): Flow<List<Task>>
    @Update
    suspend fun updateTask(task: Task)
}
