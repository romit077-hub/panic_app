package com.example.panic_app.ui.screens.panic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.screens.tasks.TasksUiState
import com.example.panic_app.ui.screens.planner.studyDuration
import com.example.panic_app.util.formatDeadline

@Composable
fun PanicScreen(state: TasksUiState, onEdit: (Long) -> Unit, onTasks: () -> Unit, onRetry: () -> Unit) {
    val attention = state.rankedPending().filter { state.risks.getValue(it.id).needsAttention }
    val highest = state.rankedPending().mapNotNull { state.risks[it.id] }.maxByOrNull { it.score }
    ScreenList {
        item { ScreenHeading("PANIC MODE", "Focus on what matters right now.") }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { Panel {
                    Text("${state.immediateCount} tasks need immediate attention", style = MaterialTheme.typography.titleLarge)
                    if (highest != null) RiskGauge(highest)
                    Text("${state.attentionCount} need planning or action • ${state.overdueCount} overdue")
                    highest?.let { Text(it.recommendedAction) }
                } }
                if (attention.isEmpty()) item { Panel {
                    Text("You're clear of immediate deadline danger.", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onTasks) { Text("View tasks") }
                } }
                itemsIndexed(attention, key = { _, task -> task.id }) { index, task ->
                    var expanded by rememberSaveable(task.id) { mutableStateOf(false) }
                    Panel(Modifier.animateItem().animateContentSize(androidx.compose.animation.core.tween(240))) {
                        Text("#${index + 1} • ${task.subject}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(task.title, style = MaterialTheme.typography.titleLarge)
                        Text(formatDeadline(task.dueDateMillis))
                        RiskSummary(state.risks.getValue(task.id))
                        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide explanation" else "Why this needs attention") }
                        AnimatedVisibility(expanded) {
                            Column {
                                Text("${studyDuration(task.estimatedMinutes.coerceAtLeast(0).toLong())} remaining • ${task.priority.label} priority")
                                Text(state.risks.getValue(task.id).reason)
                                Text(state.risks.getValue(task.id).recommendedAction, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        OutlinedButton(onClick = { onEdit(task.id) }, modifier = Modifier.fillMaxWidth()) { Text("View task") }
                    }
                }
                item { Text("Risk reflects deadlines, priority and estimated work. Keep estimates up to date as you work.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}
