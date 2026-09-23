package com.example.panic_app.domain.risk

import com.example.panic_app.model.TaskPriority
import kotlin.math.roundToInt

/**
 * Explainable heuristic, not a probability or a learned model.
 * Wall-clock time is an upper bound on time available for work (sleep/classes are not known).
 * The task estimate is treated as remaining work; the user should revise it as work progresses.
 */
object RiskCalculator {
    private const val MINUTES_PER_HOUR = 60.0
    private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
    private const val DISTANT_TIME_POINTS = 5
    private const val CAPACITY_POINTS = 50
    private const val CAPACITY_CRITICAL_FLOOR = 90
    private const val FALLBACK_ESTIMATE_MINUTES = 1

    // Deadline contribution dominates near expiry, even with LOW priority and a tiny estimate.
    private data class TimeBand(val upToMinutes: Double, val points: Int)
    private val timeBands = listOf(
        TimeBand(2 * MINUTES_PER_HOUR, 85),
        TimeBand(6 * MINUTES_PER_HOUR, 60),
        TimeBand(12 * MINUTES_PER_HOUR, 45),
        TimeBand(MINUTES_PER_DAY, 35),
        TimeBand(3 * MINUTES_PER_DAY, 20),
        TimeBand(7 * MINUTES_PER_DAY, 10)
    )
    private data class WorkBand(val upToRatio: Double, val points: Int)
    private val workBands = listOf(
        WorkBand(0.10, 0), WorkBand(0.25, 5), WorkBand(0.50, 15),
        WorkBand(0.75, 25), WorkBand(1.00, 40)
    )
    private fun priorityPoints(priority: TaskPriority): Int = when (priority) {
        TaskPriority.LOW -> 0
        TaskPriority.MEDIUM -> 5
        TaskPriority.HIGH -> 10
    }

    fun calculate(task: RiskTask, nowMillis: Long): RiskResult {
        val remaining = remainingMillis(task.dueDateMillis, nowMillis)
        if (task.isCompleted) return RiskResult(0, RiskLevel.SAFE, false, false, remaining,
            "This task is complete. Deadline risk is inactive.", "No action needed.")
        if (remaining <= 0) return expired(remaining)

        // Double division preserves sub-minute urgency; zero/negative estimates never divide by zero.
        val estimate = task.estimatedMinutes.coerceAtLeast(FALLBACK_ESTIMATE_MINUTES)
        val availableMinutes = remaining.toDouble() / MILLIS_PER_MINUTE
        val ratio = estimate.toDouble() / availableMinutes
        val time = timeBands.firstOrNull { availableMinutes <= it.upToMinutes }?.points ?: DISTANT_TIME_POINTS
        val workload = if (ratio >= 1.0) CAPACITY_POINTS else workBands.first { ratio <= it.upToRatio }.points
        val priority = priorityPoints(task.priority)
        val sum = (time + workload + priority).coerceIn(RiskLevel.MIN_SCORE, RiskLevel.MAX_SCORE)
        // If all remaining wall-clock time is consumed, even a distant deadline has no spare capacity.
        val floorApplied = ratio >= 1.0 && sum < CAPACITY_CRITICAL_FLOOR
        val score = if (floorApplied) CAPACITY_CRITICAL_FLOOR else sum
        val level = RiskLevel.fromScore(score)
        val workloadShare = when {
            ratio >= 1.0 -> "at least 100%"
            ratio < 0.01 -> "less than 1%"
            else -> "about ${(ratio * 100).roundToInt()}%"
        }
        val estimateNote = if (task.estimatedMinutes <= 0) " Missing or invalid estimate: using 1 minute; update the estimate." else ""
        val capacityNote = if (ratio >= 1.0) " Estimated work fills or exceeds the time available." else ""
        val reason = "${formatRemainingTime(remaining)}; estimated work ${formatWorkMinutes(estimate)} ($workloadShare of remaining wall-clock time). ${task.priority.label} priority." + capacityNote + estimateNote
        return RiskResult(score, level, false, true, remaining, reason, action(level),
            time, workload, priority, floorApplied)
    }

    private fun expired(remaining: Long): RiskResult {
        val overdue = remaining < 0
        return RiskResult(RiskLevel.MAX_SCORE, RiskLevel.CRITICAL, overdue, true, remaining,
            if (overdue) "${formatRemainingTime(remaining)}. This task is still incomplete." else "The deadline is now and the task is still incomplete.",
            if (overdue) "This task is overdue. Complete or reschedule it immediately." else "The deadline is now. Complete or reschedule this task immediately.",
            timePoints = RiskLevel.MAX_SCORE)
    }

    private fun action(level: RiskLevel): String = when (level) {
        RiskLevel.SAFE -> "You have enough time on paper. Keep this task scheduled."
        RiskLevel.WARNING -> "Plan a work session soon."
        RiskLevel.HIGH -> "Prioritize this task today."
        RiskLevel.CRITICAL -> "Start this task now."
    }
}
