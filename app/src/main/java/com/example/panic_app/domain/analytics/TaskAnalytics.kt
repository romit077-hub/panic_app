package com.example.panic_app.domain.analytics

import com.example.panic_app.domain.risk.*
import com.example.panic_app.model.TaskPriority
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

/** Pure, single-time snapshot shared by Dashboard, Analytics and PANIC Mode. */
object TaskAnalytics {
    private const val DAY = 24 * 60 * 60_000L
    fun calculate(tasks: List<AnalyticsTask>, nowMillis: Long, zone: ZoneId): TaskAnalysis {
        val assessed = tasks.map { AssessedTask(it.task, RiskCalculator.calculate(it.task, nowMillis)) }
        val pending = tasks.filterNot { it.task.isCompleted }
        val completed = tasks.size - pending.size
        val risks = assessed.associate { it.task.id to it.risk }
        val distribution = RiskLevel.entries.associateWith { level ->
            assessed.count { it.risk.isActive && it.risk.level == level }
        }
        // Local calendar midnight handles 23/25-hour DST days. Other periods are rolling durations.
        val midnight = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant()
        val endToday = if (midnight > Instant.ofEpochMilli(Long.MAX_VALUE)) Long.MAX_VALUE else midnight.toEpochMilli()
        fun window(end: Long, inclusive: Boolean = true): WorkloadPeriod {
            val due = pending.filter { it.task.dueDateMillis >= nowMillis &&
                if (inclusive) it.task.dueDateMillis <= end else it.task.dueDateMillis < end }
            return WorkloadPeriod(due.size, due.sumOf { it.task.estimatedMinutes.coerceAtLeast(0).toLong() })
        }
        val nearest = pending.minWithOrNull(compareBy<AnalyticsTask> { it.task.dueDateMillis }.thenBy { it.task.id })
        val fraction = if (tasks.isEmpty()) 0.0 else completed.toDouble() / tasks.size
        val summary = AnalyticsSummary(
            total = tasks.size, pending = pending.size, completed = completed,
            overdue = assessed.count { it.risk.isOverdue },
            completionPercent = if (tasks.isEmpty()) null else (fraction * 100).roundToInt().coerceIn(0, 100),
            completionFraction = fraction.toFloat().coerceIn(0f, 1f),
            pendingMinutes = pending.sumOf { it.task.estimatedMinutes.coerceAtLeast(0).toLong() },
            remainingToday = window(endToday, inclusive = false), next24Hours = window(end(nowMillis, DAY)),
            next3Days = window(end(nowMillis, 3 * DAY)), next7Days = window(end(nowMillis, 7 * DAY)),
            nearest = nearest?.let { PendingDeadline(it.task.id, it.title, it.task.dueDateMillis) },
            riskCounts = distribution,
            priorityCounts = TaskPriority.entries.associateWith { priority -> pending.count { it.task.priority == priority } })
        return TaskAnalysis(summary.copy(insights = ProductivityInsights.from(summary)), risks,
            RiskRanking.pending(assessed).map { it.task.id })
    }
    private fun end(now: Long, duration: Long): Long = if (now > Long.MAX_VALUE - duration) Long.MAX_VALUE else now + duration
}
