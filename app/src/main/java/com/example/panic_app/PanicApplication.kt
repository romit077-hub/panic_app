package com.example.panic_app

import android.app.Application
import androidx.room.Room
import com.example.panic_app.data.local.PanicDatabase
import com.example.panic_app.data.repository.TaskRepository

class PanicApplication : Application() {
    // One database/repository per application process. No automatic demo insertion.
    private val database by lazy {
        Room.databaseBuilder(applicationContext, PanicDatabase::class.java, "panic.db").build()
    }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
}
