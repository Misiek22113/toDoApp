package com.example.todoapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.util.Converters
import com.example.todoapp.dao.TaskDao
import com.example.todoapp.model.Task

@Database(
    entities = [Task::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract val dao: TaskDao
}