package com.example.panic_app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.panic_app.model.TaskPriority

@Entity(tableName = "tasks", indices = [Index(value = ["isCompleted", "dueDateMillis"])])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String,
    val dueDateMillis: Long,
    val estimatedMinutes: Int,
    val priority: TaskPriority,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
