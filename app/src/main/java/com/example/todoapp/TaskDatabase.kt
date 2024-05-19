package com.example.todoapp

import com.example.todoapp.Converters;
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract val dao: TaskDao
}