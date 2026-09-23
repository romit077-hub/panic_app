package com.example.panic_app.domain.analytics

import com.example.panic_app.domain.risk.RiskTask
import com.example.panic_app.domain.risk.RiskLevel
import com.example.panic_app.domain.risk.RiskResult
import com.example.panic_app.model.TaskPriority

data class AnalyticsTask(val task: RiskTask, val title: String)
data class WorkloadPeriod(val count: Int = 0, val minutes: Long = 0)
data class PendingDeadline(val id: Long, val title: String, val dueDateMillis: Long)
data class AnalyticsSummary(
    val total: Int = 0, val pending: Int = 0, val completed: Int = 0, val overdue: Int = 0,
    val completionPercent: Int? = null, val completionFraction: Float = 0f,
    val pendingMinutes: Long = 0, val remainingToday: WorkloadPeriod = WorkloadPeriod(),
    val next24Hours: WorkloadPeriod = WorkloadPeriod(), val next3Days: WorkloadPeriod = WorkloadPeriod(),
    val next7Days: WorkloadPeriod = WorkloadPeriod(), val nearest: PendingDeadline? = null,
    val riskCounts: Map<RiskLevel, Int> = RiskLevel.entries.associateWith { 0 },
    val priorityCounts: Map<TaskPriority, Int> = TaskPriority.entries.associateWith { 0 },
    val insights: List<ProductivityInsight> = emptyList()
) {
    val high: Int get() = riskCounts.getValue(RiskLevel.HIGH)
    val critical: Int get() = riskCounts.getValue(RiskLevel.CRITICAL)
}
data class TaskAnalysis(val summary: AnalyticsSummary, val risks: Map<Long, RiskResult>, val rankedPendingIds: List<Long>)

fun formatWorkload(minutes: Long): String {
    val safe = minutes.coerceAtLeast(0)
    val hours = safe / 60
    val remainder = safe % 60
    return when {
        hours == 0L -> "$remainder min"
        remainder == 0L -> "${hours}h"
        else -> "${hours}h ${remainder}m"
    }
}
