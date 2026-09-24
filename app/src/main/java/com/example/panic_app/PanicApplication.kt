package com.example.panic_app

import android.app.Application
import androidx.room.Room
import com.example.panic_app.data.local.PanicDatabase
import com.example.panic_app.data.repository.TaskRepository
import com.example.panic_app.data.repository.TaskMutationGate
import com.example.panic_app.notification.DeadlineReminderController

class PanicApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, PanicDatabase::class.java, "panic.db").build()
    }
    val plannerSettings by lazy { com.example.panic_app.data.settings.PlannerSettingsStore(this) }
    private val mutationGate = TaskMutationGate()
    val reminders by lazy { DeadlineReminderController(this, database.taskDao(), mutationGate) }
    val taskRepository by lazy { TaskRepository(database.taskDao(), mutationGate) }
    override fun onCreate() {
        super.onCreate()
        reminders.start()
    }
}
