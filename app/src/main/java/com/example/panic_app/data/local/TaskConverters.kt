package com.example.panic_app.data.local

import androidx.room.TypeConverter
import com.example.panic_app.model.TaskPriority

class TaskConverters {
    @TypeConverter fun toPriority(value: String): TaskPriority = TaskPriority.valueOf(value)
    @TypeConverter fun fromPriority(value: TaskPriority): String = value.name
}
