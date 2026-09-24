package com.example.panic_app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.screens.tasks.TasksUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(state: TasksUiState, onTasks: () -> Unit, onPanic: () -> Unit,
    onAnalytics: () -> Unit, onSettings: () -> Unit, onAdd: () -> Unit, onEdit: (Long) -> Unit, onRetry: () -> Unit, onPlan: () -> Unit) {
    val pending = state.rankedPending()
    val nearest = state.analytics.nearest?.let { deadline -> pending.firstOrNull { it.id == deadline.id } }
    val attention = pending.firstOrNull { state.risks.getValue(it.id).needsAttention }
    ScreenList {
        item {
            val greeting = when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
            ScreenHeading(greeting, "Let's keep those deadlines under control.")
        }
        item { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("+ Add Task") } }
        item { Panel {
            Text("Smart Planner", style = MaterialTheme.typography.titleMedium)
            Text("Build today's study plan around your deadlines.")
            OutlinedButton(onClick = onPlan, modifier = Modifier.fillMaxWidth()) { Text("Open Smart Planner") }
        } }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Pending", state.pendingCount.toString(), Modifier.weight(1f))
                        StatCard("Completed", state.completedCount.toString(), Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("High risk", state.highCount.toString(), Modifier.weight(1f))
                        StatCard("Critical", state.criticalCount.toString(), Modifier.weight(1f))
                    }
                }
                item { SectionHeader("Needs attention", "Panic Mode", onPanic) }
                if (attention != null) item {
                    Panel {
                        Text(attention.title, style = MaterialTheme.typography.titleLarge)
                        Text(attention.subject, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        RiskSummary(state.risks.getValue(attention.id), expanded = true)
                        TextButton(onClick = { onEdit(attention.id) }) { Text("View / edit task") }
                    }
                } else item { Panel { Text("Everything is under control.", style = MaterialTheme.typography.titleMedium); Text("No deadlines currently need urgent attention.") } }
                item { SectionHeader("Nearest pending deadline", "All tasks", onTasks) }
                if (nearest != null) item { DeadlineCard(nearest, state.risks.getValue(nearest.id), onEdit = { onEdit(nearest.id) }) }
                else item { EmptyTasks(
                    title = if (state.tasks.isEmpty()) "No deadlines yet" else "You're all caught up",
                    subtitle = if (state.tasks.isEmpty()) "Add your first task and PANIC will help you stay ahead." else "All your saved tasks are complete.", onAdd = onAdd) }
                item {
                    Panel {
                        Text("PANIC MODE", style = MaterialTheme.typography.titleLarge)
                        Text("${state.attentionCount} tasks need planning or action.")
                        Text("${state.immediateCount} need immediate attention • ${state.overdueCount} overdue", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = onPanic, modifier = Modifier.fillMaxWidth()) { Text("Enter Panic Mode") }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onAnalytics, modifier = Modifier.weight(1f)) { Text("Analytics") }
                OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("Settings") }
            }
        }
    }
}
