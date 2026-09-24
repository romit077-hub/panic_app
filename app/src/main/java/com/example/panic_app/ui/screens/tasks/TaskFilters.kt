package com.example.panic_app.ui.screens.tasks

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.risk.RiskLevel
import java.time.Instant
import java.time.ZoneId

enum class TaskFilter(val label: String) { Pending("Pending"), Today("Today"), Upcoming("Upcoming"), Critical("Critical"), Completed("Completed"), All("All") }
fun filterTasks(state: TasksUiState, filter: TaskFilter, now: Long, zone: ZoneId): List<TaskEntity> {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val pending = state.rankedPending()
    fun date(task: TaskEntity) = Instant.ofEpochMilli(task.dueDateMillis).atZone(zone).toLocalDate()
    return when (filter) {
        TaskFilter.Pending -> pending
        TaskFilter.Today -> pending.filter { date(it) == today }
        TaskFilter.Upcoming -> pending.filter { date(it) > today }
        TaskFilter.Critical -> pending.filter { state.risks[it.id]?.level == RiskLevel.CRITICAL }
        TaskFilter.Completed -> state.tasks.filter { it.isCompleted }
        TaskFilter.All -> pending + state.tasks.filter { it.isCompleted }
    }
}
