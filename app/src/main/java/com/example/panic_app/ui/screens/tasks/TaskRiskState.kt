package com.example.panic_app.ui.screens.tasks

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.risk.RiskTask
import com.example.panic_app.domain.analytics.AnalyticsTask
import com.example.panic_app.domain.analytics.TaskAnalytics
import java.time.ZoneId

/** One atomic task/risk/analytics snapshot per Room emission or foreground time tick. */
internal fun TasksUiState.withRisk(tasks: List<TaskEntity>, nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()): TasksUiState {
    val analysis = TaskAnalytics.calculate(tasks.map { task ->
        AnalyticsTask(RiskTask(task.id, task.dueDateMillis, task.estimatedMinutes, task.priority, task.isCompleted), task.title)
    }, nowMillis, zone)
    return copy(tasks = tasks, calculatedAtMillis = nowMillis, analytics = analysis.summary,
        risks = analysis.risks, rankedPendingIds = analysis.rankedPendingIds)
}
