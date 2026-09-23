package com.example.panic_app.domain.analytics

enum class InsightKind { OVERDUE, CRITICAL, WORKLOAD, PROGRESS, ALL_CLEAR, EMPTY }
data class ProductivityInsight(val kind: InsightKind, val message: String)

/** Ordered rules, capped at four. No historical trends or AI claims. */
object ProductivityInsights {
    fun from(summary: AnalyticsSummary): List<ProductivityInsight> = buildList {
        if (summary.total == 0) {
            add(ProductivityInsight(InsightKind.EMPTY, "Add your first task to see workload and completion insights."))
            return@buildList
        }
        if (summary.overdue > 0) add(ProductivityInsight(InsightKind.OVERDUE,
            "${summary.overdue} overdue task(s). Review them in PANIC Mode."))
        // Overdue tasks are already critical; avoid describing the same overdue problem twice.
        val upcomingCritical = summary.critical - summary.overdue
        if (upcomingCritical > 0) add(ProductivityInsight(InsightKind.CRITICAL,
            "$upcomingCritical critical task(s) at or before their deadline need immediate attention."))
        if (summary.next24Hours.count > 0) add(ProductivityInsight(InsightKind.WORKLOAD,
            "${formatWorkload(summary.next24Hours.minutes)} of estimated work is due within 24 hours across ${summary.next24Hours.count} task(s)."))
        if (summary.completed > 0) add(ProductivityInsight(InsightKind.PROGRESS,
            "You've completed ${summary.completed} of your ${summary.total} recorded tasks."))
        if (summary.pending == 0) add(ProductivityInsight(InsightKind.ALL_CLEAR, "You're all caught up. No pending tasks remain."))
        else if (isEmpty()) add(ProductivityInsight(InsightKind.WORKLOAD,
            "${summary.pending} pending task(s), with ${formatWorkload(summary.pendingMinutes)} of estimated work. Keep them scheduled."))
    }.take(4)
}
