package com.example.panic_app.notification

import android.content.Context
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small settings and delivery ledger. All writes run on IO under TaskMutationGate. */
class ReminderStore(context: Context) {
    private val preferences = context.getSharedPreferences("deadline_reminders", Context.MODE_PRIVATE)
    private val mutableSettings = MutableStateFlow(readSettings())
    val settings = mutableSettings.asStateFlow()
    private fun readSettings() = ReminderSettings(
        preferences.getBoolean("enabled", true), preferences.getBoolean("panic", true),
        ReminderIntensity.entries.firstOrNull { it.name == preferences.getString("intensity", null) }
            ?: ReminderIntensity.BALANCED)
    fun save(settings: ReminderSettings) {
        persist(preferences.edit().putBoolean("enabled", settings.enabled)
            .putBoolean("panic", settings.panicAlerts).putString("intensity", settings.intensity.name))
        mutableSettings.value = settings
    }
    fun last(id: Long): ReminderStamp? {
        val value = preferences.getString("task:$id", null)?.split('|') ?: return null
        val severity = ReminderSeverity.entries.firstOrNull { it.name == value.firstOrNull() } ?: return null
        val time = value.getOrNull(1)?.toLongOrNull() ?: return null
        return ReminderStamp(severity, time)
    }
    fun lastGlobal(): Long? = if (preferences.contains("last_global")) preferences.getLong("last_global", 0) else null
    fun reserve(id: Long, stamp: ReminderStamp) {
        // Commit before posting: a process death cannot cause a repeated alert on the next run.
        persist(preferences.edit().putString("task:$id", "${stamp.severity.name}|${stamp.timestamp}")
            .putLong("last_global", stamp.timestamp))
    }
    fun prune(existingIds: Set<Long>) {
        val obsolete = preferences.all.keys.filter { it.startsWith("task:") && it.removePrefix("task:").toLongOrNull() !in existingIds }
        if (obsolete.isNotEmpty()) {
            val edit = preferences.edit(); obsolete.forEach { edit.remove(it) }; persist(edit)
        }
    }
    private fun persist(editor: android.content.SharedPreferences.Editor) {
        if (!editor.commit()) throw IOException("Could not save reminder preferences. Please try again.")
    }
}
