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
    onAnalytics: () -> Unit, onSettings: () -> Unit, onAdd: () -> Unit, onEdit: (Long) -> Unit, onRetry: () -> Unit) {
    val pending = state.tasks.filterNot { it.isCompleted }
    val nearest = pending.minByOrNull { it.dueDateMillis }
    ScreenList {
        item {
            val greeting = when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
            ScreenHeading(greeting, "Let's keep those deadlines under control.")
        }
        item { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("+ Add Task") } }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Pending", pending.size.toString(), Modifier.weight(1f))
                        StatCard("Completed", state.tasks.count { it.isCompleted }.toString(), Modifier.weight(1f))
                    }
                }
                item { SectionHeader("Nearest pending deadline", "All tasks", onTasks) }
                if (nearest != null) item { DeadlineCard(nearest, onEdit = { onEdit(nearest.id) }) }
                else item { EmptyTasks(
                    title = if (state.tasks.isEmpty()) "No deadlines yet" else "You're all caught up",
                    subtitle = if (state.tasks.isEmpty()) "Add your first task and PANIC will help you stay ahead." else "All your saved tasks are complete.", onAdd = onAdd) }
            }
        }
        item {
            Panel {
                Text("PANIC MODE", style = MaterialTheme.typography.titleLarge)
                Text("Risk scoring is coming in a later phase. Explore the clearly labelled design preview.")
                OutlinedButton(onClick = onPanic, modifier = Modifier.fillMaxWidth()) { Text("Open Panic preview") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onAnalytics, modifier = Modifier.weight(1f)) { Text("Analytics preview") }
                OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("Settings") }
            }
        }
    }
}
