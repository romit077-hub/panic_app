package com.example.panic_app.notification

import android.content.Context
import com.example.panic_app.data.local.TaskDao
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.data.repository.TaskMutationGate
import com.example.panic_app.domain.risk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Manual application-scoped wiring. Worker and debug check use this exact pipeline. */
class DeadlineReminderController(context: Context, private val dao: TaskDao, private val gate: TaskMutationGate) {
    private val store = ReminderStore(context)
    val settings = store.settings
    val notifications = PanicNotificationManager(context)
    private val scheduler = NotificationScheduler(context)
    init {
        gate.onChanged = { id ->
            notifications.cancel(id)
            if (settings.value.enabled) scheduler.checkSoon()
        }
    }
    fun start() { scheduler.configure(settings.value.enabled) }
    suspend fun updateSettings(transform: (ReminderSettings) -> ReminderSettings) = withContext(Dispatchers.IO) {
        gate.mutex.withLock {
            val updated = transform(settings.value)
            store.save(updated)
            if (!updated.enabled) notifications.cancelAll()
            else if (!updated.panicAlerts) notifications.cancelUrgent()
            scheduler.configure(updated.enabled)
            if (updated.enabled) scheduler.checkSoon()
        }
    }
    suspend fun check(): String = withContext(Dispatchers.IO) {
        gate.mutex.withLock {
            val all = dao.observeAll().first()
            val pendingIds = all.filterNot { it.isCompleted }.map { it.id }.toSet()
            notifications.cancelInactive(pendingIds)
            store.prune(all.map { it.id }.toSet())
            if (!settings.value.enabled) {
                notifications.cancelAll()
                return@withLock "Notifications are off in PANIC settings."
            }
            if (!notifications.available()) return@withLock "Android has blocked notifications. Check system settings."
            val now = System.currentTimeMillis()
            val candidates = all.filter { it.id in pendingIds }.map { task ->
                task to RiskCalculator.calculate(RiskTask(task.id, task.dueDateMillis, task.estimatedMinutes,
                    task.priority, task.isCompleted), now)
            }.sortedWith(compareByDescending<Pair<TaskEntity, RiskResult>> { it.second.isOverdue }
                .thenByDescending { it.second.score }.thenBy { it.first.dueDateMillis }.thenBy { it.first.id })
            candidates.filter { !ReminderPolicy.allowed(ReminderPolicy.severity(it.second), settings.value) }
                .forEach { notifications.cancel(it.first.id) }
            for ((task, risk) in candidates) {
                val severity = ReminderPolicy.severity(risk)
                if (!ReminderPolicy.allowed(severity, settings.value)) continue
                if (severity == null || !notifications.channelAvailable(severity)) continue
                if (!ReminderPolicy.shouldNotify(risk, settings.value, store.last(task.id), now, store.lastGlobal())) continue
                // Do not allow cancellation between durable reservation and the Android post.
                // A crash in this tiny interval may skip one alert, never duplicate it.
                return@withLock withContext(NonCancellable) {
                    store.reserve(task.id, ReminderStamp(severity, now))
                    if (notifications.post(task, risk, severity)) "Reminder posted for ${task.title}."
                    else "Android blocked this reminder. Preferences and cooldowns remain active."
                }
            }
            "Check complete. No alert eligible (risk, preferences, channel settings or cooldown)."
        }
    }
}
