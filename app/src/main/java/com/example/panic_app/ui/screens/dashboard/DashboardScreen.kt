package com.example.panic_app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.screens.tasks.TasksUiState
import com.example.panic_app.ui.screens.planner.*
import com.example.panic_app.util.formatDeadline
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(state: TasksUiState, planner: PlannerUiState, onTasks: () -> Unit, onPanic: () -> Unit,
    onAnalytics: () -> Unit, onSettings: () -> Unit, onAdd: () -> Unit, onEdit: (Long) -> Unit,
    onRetry: () -> Unit, onPlan: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val now = state.calculatedAtMillis
    val local = Instant.ofEpochMilli(now).atZone(zone)
    val pending = state.rankedPending()
    val next = pending.firstOrNull()
    val highest = pending.mapNotNull { state.risks[it.id] }.maxByOrNull { it.score }
    val todayTasks = pending.filter { plannerDate(it.dueDateMillis, zone) == local.toLocalDate() }
    ScreenList {
        item {
            val greeting = when (local.hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
            ScreenHeading(greeting, "A clear view of what needs your attention.")
            if (!state.loading) Text(local.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.labelLarge)
        }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { Panel {
                    Text("DUE TODAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text("${todayTasks.size} tasks • ${studyDuration(todayTasks.sumOf { it.estimatedMinutes.coerceAtLeast(0).toLong() })} estimated work",
                        style = MaterialTheme.typography.titleLarge)
                    Text("${state.criticalCount} critical overall • ${state.overdueCount} overdue", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (highest != null) RiskGauge(highest)
                    else Text("Nothing is chasing you right now.")
                } }
                item { SectionHeader("Next action", "All tasks", onTasks) }
                if (next != null) item { Panel {
                    Text(next.title, style = MaterialTheme.typography.titleLarge)
                    RiskSummary(state.risks.getValue(next.id))
                    Text("${studyDuration(next.estimatedMinutes.coerceAtLeast(0).toLong())} remaining • ${next.priority.label} priority")
                    Button(onClick = { onEdit(next.id) }, modifier = Modifier.fillMaxWidth()) { Text("Open task") }
                } } else item { EmptyTasks("You're all caught up", "Add a deadline when you're ready for what's next.", onAdd) }
                item { SmartPlanPanel(planner, now, zone, onPlan) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Pending", state.pendingCount.toString(), Modifier.weight(1f))
                    StatCard("Completed", state.completedCount.toString(), Modifier.weight(1f))
                } }
                item { Panel {
                    SectionHeader("Deadline attention", "Panic Mode", onPanic)
                    Text("${state.immediateCount} tasks need immediate attention.")
                    state.analytics.nearest?.let { Text("Nearest deadline: ${it.title}\n${formatDeadline(it.dueDateMillis)}") }
                } }
            }
        }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("+ Add task") } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAnalytics, modifier = Modifier.weight(1f)) { Text("Analytics") }
            OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("Settings") }
        } }
    }
}

@Composable
private fun SmartPlanPanel(state: PlannerUiState, now: Long, zone: ZoneId, onPlan: () -> Unit) {
    val plan = state.plan
    Panel {
        Text("SMART PLAN", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        if (state.generating) Text("Building your plan…")
        else if (plan == null) Text("Build a realistic study plan around your deadlines.")
        else {
            val today = plannerDate(now, zone)
            Text("${studyDuration(plan.sessions.filter { plannerDate(it.startTime, zone) == today }.sumOf { it.durationMinutes.toLong() })} scheduled today",
                style = MaterialTheme.typography.titleLarge)
            plan.sessions.firstOrNull { it.endTime > now }?.let {
                Text("${if (it.startTime <= now) "Current session" else "Next session"}: ${it.taskTitle}")
                Text(formatDeadline(it.startTime, zone, today), style = MaterialTheme.typography.bodySmall)
            }
            if (plannerDate(plan.generatedAt, zone) != today || state.zone != zone) Text("Open your plan to refresh today's schedule.")
        }
        FilledTonalButton(onClick = onPlan, modifier = Modifier.fillMaxWidth()) { Text(if (plan == null) "Build my plan" else "View plan") }
    }
}
