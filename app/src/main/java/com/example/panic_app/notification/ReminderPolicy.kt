package com.example.panic_app.notification

import com.example.panic_app.model.ThemePreference
import com.example.panic_app.domain.risk.RiskLevel
import com.example.panic_app.domain.risk.RiskResult

enum class ReminderIntensity { GENTLE, BALANCED, AGGRESSIVE }
enum class ReminderSeverity { WARNING, HIGH, CRITICAL, OVERDUE }
data class ReminderSettings(val enabled: Boolean = true, val panicAlerts: Boolean = true,
    val intensity: ReminderIntensity = ReminderIntensity.BALANCED,
    val theme: ThemePreference = ThemePreference.SYSTEM)
data class ReminderStamp(val severity: ReminderSeverity, val timestamp: Long)

/** Pure policy. RiskCalculator remains the sole source of risk classification. */
object ReminderPolicy {
    const val MINUTE = 60_000L
    const val ESCALATION_GAP = 30 * MINUTE
    const val GLOBAL_GAP = 5 * MINUTE
    fun severity(risk: RiskResult): ReminderSeverity? = when {
        !risk.isActive -> null
        risk.isOverdue -> ReminderSeverity.OVERDUE
        else -> when (risk.level) {
            RiskLevel.SAFE -> null
            RiskLevel.WARNING -> ReminderSeverity.WARNING
            RiskLevel.HIGH -> ReminderSeverity.HIGH
            RiskLevel.CRITICAL -> ReminderSeverity.CRITICAL
        }
    }
    fun cooldown(severity: ReminderSeverity, intensity: ReminderIntensity): Long {
        val hours = when (intensity) {
            ReminderIntensity.GENTLE -> when (severity) {
                ReminderSeverity.WARNING -> 24; ReminderSeverity.HIGH -> 12
                ReminderSeverity.CRITICAL -> 6; ReminderSeverity.OVERDUE -> 24
            }
            ReminderIntensity.BALANCED -> when (severity) {
                ReminderSeverity.WARNING -> 12; ReminderSeverity.HIGH -> 6
                ReminderSeverity.CRITICAL -> 3; ReminderSeverity.OVERDUE -> 12
            }
            ReminderIntensity.AGGRESSIVE -> when (severity) {
                ReminderSeverity.WARNING -> 6; ReminderSeverity.HIGH -> 3
                ReminderSeverity.CRITICAL -> 1; ReminderSeverity.OVERDUE -> 6
            }
        }
        return hours * 60 * MINUTE
    }
    fun allowed(severity: ReminderSeverity?, settings: ReminderSettings): Boolean =
        settings.enabled && severity != null &&
            (settings.panicAlerts || severity == ReminderSeverity.WARNING) &&
            (settings.intensity != ReminderIntensity.GENTLE || severity != ReminderSeverity.WARNING)

    fun shouldNotify(risk: RiskResult, settings: ReminderSettings, last: ReminderStamp?,
        now: Long, lastGlobal: Long? = null): Boolean {
        val severity = severity(risk) ?: return false
        if (!allowed(severity, settings)) return false
        // Clock rollback waits for the stored time to catch up; it never creates a notification burst.
        if (lastGlobal != null && !elapsed(now, lastGlobal, GLOBAL_GAP)) return false
        if (last == null) return true
        val gap = if (severity.ordinal > last.severity.ordinal) ESCALATION_GAP else cooldown(severity, settings.intensity)
        return elapsed(now, last.timestamp, gap)
    }
    private fun elapsed(now: Long, previous: Long, gap: Long): Boolean =
        now >= previous && (now.toDouble() - previous.toDouble()) >= gap
}
