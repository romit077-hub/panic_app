package com.example.panic_app.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.panic_app.MainActivity
import com.example.panic_app.R
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.risk.*

class PanicNotificationManager(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    init { NotificationChannels.create(manager) }
    fun available(): Boolean = (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context,
        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    fun channelAvailable(severity: ReminderSeverity): Boolean = available() &&
        manager.getNotificationChannel(NotificationChannels.forSeverity(severity))?.importance != NotificationManager.IMPORTANCE_NONE
    fun status(): String = when {
        !available() -> "Android notifications are blocked. Allow them below or in system settings."
        !channelAvailable(ReminderSeverity.WARNING) && !channelAvailable(ReminderSeverity.HIGH) -> "Both notification channels are blocked in Android settings."
        !channelAvailable(ReminderSeverity.WARNING) -> "Deadline Reminders are blocked in Android settings."
        !channelAvailable(ReminderSeverity.HIGH) -> "Panic Alerts are blocked in Android settings."
        else -> "Android allows both channels. Delivery follows your preferences and cooldowns."
    }
    fun cancel(id: Long) { manager.cancel(tag(id), NOTIFICATION_ID) }
    fun cancelInactive(pendingIds: Set<Long>) {
        manager.activeNotifications.filter { item ->
            val taskId = item.tag?.takeIf { it.startsWith("deadline:") }?.removePrefix("deadline:")?.toLongOrNull()
            taskId != null && taskId !in pendingIds
        }.forEach { manager.cancel(it.tag, it.id) }
    }
    fun cancelAll() { manager.cancelAll() }
    fun cancelUrgent() {
        manager.activeNotifications.filter { it.notification.channelId == NotificationChannels.PANIC }
            .forEach { manager.cancel(it.tag, it.id) }
    }
    fun post(task: TaskEntity, risk: RiskResult, severity: ReminderSeverity): Boolean {
        if (!channelAvailable(severity)) return false
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_PANIC
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val title = when (severity) {
            ReminderSeverity.WARNING -> "Deadline approaching"
            ReminderSeverity.HIGH -> "PANIC — High-risk deadline"
            ReminderSeverity.CRITICAL -> "PANIC — Start now"
            ReminderSeverity.OVERDUE -> "PANIC — Deadline missed"
        }
        val body = "${task.title} • ${formatRemainingTime(risk.remainingMillis)}. " +
            "Estimated work: ${formatWorkMinutes(task.estimatedMinutes)}. ${risk.recommendedAction}"
        val notification = NotificationCompat.Builder(context, NotificationChannels.forSeverity(severity))
            .setSmallIcon(R.drawable.ic_notification_deadline).setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)).setContentIntent(pending)
            .setAutoCancel(true).setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        return try { manager.notify(tag(task.id), NOTIFICATION_ID, notification); true }
        catch (_: SecurityException) { false } // Permission can be revoked after the check.
    }
    private fun tag(id: Long) = "deadline:$id" // No Long-to-Int collisions; one slot per task.
    companion object {
        const val ACTION_PANIC = "com.example.panic_app.OPEN_PANIC"
        private const val NOTIFICATION_ID = 1
    }
}
