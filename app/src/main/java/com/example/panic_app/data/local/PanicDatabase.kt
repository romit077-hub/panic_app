package com.example.panic_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
@TypeConverters(TaskConverters::class)
abstract class PanicDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
