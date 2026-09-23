package com.example.panic_app.notification

import com.example.panic_app.domain.risk.*
import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test

class ReminderPolicyTest {
    private val now = 1_800_000_000_000L
    private val balanced = ReminderSettings()
    private val hour = 60 * ReminderPolicy.MINUTE
    private fun risk(level: RiskLevel, overdue: Boolean = false, active: Boolean = true) =
        RiskResult(when (level) { RiskLevel.SAFE -> 10; RiskLevel.WARNING -> 45; RiskLevel.HIGH -> 70; RiskLevel.CRITICAL -> 90 },
            level, overdue, active, if (overdue) -hour else hour, "Reason", "Action")
    private fun stamp(level: ReminderSeverity, ago: Long) = ReminderStamp(level, now - ago)

    @Test fun safeDoesNotNotify() { assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.SAFE), balanced, null, now)) }
    @Test fun warningFirstEntryNotifies() { assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.WARNING), balanced, null, now)) }
    @Test fun highFirstEntryNotifies() { assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.HIGH), balanced, null, now)) }
    @Test fun criticalFirstEntryNotifies() { assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, null, now)) }
    @Test fun overdueHasDistinctSeverity() {
        val risk = risk(RiskLevel.CRITICAL, overdue = true)
        assertEquals(ReminderSeverity.OVERDUE, ReminderPolicy.severity(risk))
        assertTrue(ReminderPolicy.shouldNotify(risk, balanced, null, now))
    }
    @Test fun completedHasNoReminderEvenWithOldDeadline() {
        val result = RiskCalculator.calculate(RiskTask(1, now - hour, 60, TaskPriority.HIGH, true), now)
        assertFalse(ReminderPolicy.shouldNotify(result, balanced, null, now))
    }
    @Test fun repeatedCriticalIsThrottled() {
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, stamp(ReminderSeverity.CRITICAL, hour), now))
    }
    @Test fun warningToHighEscalatesAfterMinimumGap() {
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.HIGH), balanced, stamp(ReminderSeverity.WARNING, ReminderPolicy.ESCALATION_GAP), now))
    }
    @Test fun highToCriticalEscalatesAfterMinimumGap() {
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, stamp(ReminderSeverity.HIGH, ReminderPolicy.ESCALATION_GAP), now))
    }
    @Test fun immediateEscalationStillThrottled() {
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, stamp(ReminderSeverity.HIGH, ReminderPolicy.MINUTE), now))
    }
    @Test fun disabledPreferenceBlocksAllSeverities() {
        RiskLevel.entries.forEach { assertFalse(ReminderPolicy.shouldNotify(risk(it), balanced.copy(enabled = false), null, now)) }
    }
    @Test fun panicPreferenceBlocksUrgentButAllowsWarning() {
        val prefs = balanced.copy(panicAlerts = false)
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.WARNING), prefs, null, now))
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.HIGH), prefs, null, now))
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL, overdue = true), prefs, null, now))
    }
    @Test fun gentleSkipsWarning() {
        val prefs = balanced.copy(intensity = ReminderIntensity.GENTLE)
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.WARNING), prefs, null, now))
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.HIGH), prefs, null, now))
    }
    @Test fun aggressiveHasShorterButNonzeroCooldown() {
        val last = stamp(ReminderSeverity.CRITICAL, hour)
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced.copy(intensity = ReminderIntensity.AGGRESSIVE), last, now))
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, last, now))
    }
    @Test fun exactCooldownBoundaryAllowsRepeat() {
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, stamp(ReminderSeverity.CRITICAL, 3 * hour), now))
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, stamp(ReminderSeverity.CRITICAL, 3 * hour - 1), now))
    }
    @Test fun overdueEscalatesFromCritical() {
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL, overdue = true), balanced,
            stamp(ReminderSeverity.CRITICAL, hour), now))
    }
    @Test fun globalThrottleAlsoAppliesToNewTasks() {
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, null, now, now - ReminderPolicy.MINUTE))
        assertTrue(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, null, now, now - ReminderPolicy.GLOBAL_GAP))
    }
    @Test fun clockRollbackDoesNotBurst() {
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, ReminderStamp(ReminderSeverity.WARNING, now + hour), now))
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.CRITICAL), balanced, null, now, now + hour))
    }
    @Test fun riskDowngradeDoesNotBypassCooldown() {
        assertFalse(ReminderPolicy.shouldNotify(risk(RiskLevel.WARNING), balanced, stamp(ReminderSeverity.CRITICAL, hour), now))
    }
    @Test fun realCalculatorDrivesUrgency() {
        val task = RiskTask(7, now + hour, 120, TaskPriority.HIGH)
        val result = RiskCalculator.calculate(task, now)
        assertEquals(ReminderSeverity.CRITICAL, ReminderPolicy.severity(result))
        assertTrue(ReminderPolicy.shouldNotify(result, balanced, null, now))
    }
}
