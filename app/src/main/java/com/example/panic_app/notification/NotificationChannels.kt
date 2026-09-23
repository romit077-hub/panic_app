package com.example.panic_app.notification

import android.app.NotificationChannel
import android.app.NotificationManager

object NotificationChannels {
    const val DEADLINES = "deadline_reminders"
    const val PANIC = "panic_alerts"
    fun create(manager: NotificationManager) {
        manager.createNotificationChannels(listOf(
            NotificationChannel(DEADLINES, "Deadline Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Planning reminders for deadlines at warning risk."
            },
            NotificationChannel(PANIC, "Panic Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "High-risk, critical and overdue task reminders."
            }
        ))
    }
    fun forSeverity(severity: ReminderSeverity) = if (severity == ReminderSeverity.WARNING) DEADLINES else PANIC
}
