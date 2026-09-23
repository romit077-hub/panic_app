package com.example.panic_app.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(context: Context) {
    private val work = WorkManager.getInstance(context)
    fun configure(enabled: Boolean) {
        if (enabled) work.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DeadlineCheckWorker>(15, TimeUnit.MINUTES).build())
        else {
            work.cancelUniqueWork(PERIODIC)
            work.cancelUniqueWork(IMMEDIATE)
        }
    }
    fun checkSoon() {
        work.enqueueUniqueWork(IMMEDIATE, ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DeadlineCheckWorker>().build())
    }
    companion object {
        private const val PERIODIC = "panic-deadline-periodic"
        private const val IMMEDIATE = "panic-deadline-refresh"
    }
}
