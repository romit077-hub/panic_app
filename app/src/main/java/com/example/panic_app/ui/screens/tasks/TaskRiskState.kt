package com.example.panic_app.ui.screens.tasks

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.risk.*

/** Derives an atomic presentation snapshot from Room's list; nothing is written back to Room. */
internal fun TasksUiState.withRisk(tasks: List<TaskEntity>, nowMillis: Long): TasksUiState {
    val assessed = tasks.map { task ->
        val input = RiskTask(task.id, task.dueDateMillis, task.estimatedMinutes, task.priority, task.isCompleted)
        AssessedTask(input, RiskCalculator.calculate(input, nowMillis))
    }
    return copy(tasks = tasks, calculatedAtMillis = nowMillis,
        risks = assessed.associate { it.task.id to it.risk },
        rankedPendingIds = RiskRanking.pending(assessed).map { it.task.id })
}
