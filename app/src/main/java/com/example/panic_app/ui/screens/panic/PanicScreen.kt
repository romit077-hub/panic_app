package com.example.panic_app.ui.screens.panic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panic_app.domain.risk.formatWorkMinutes
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.screens.tasks.TasksUiState
import com.example.panic_app.util.formatDeadline

@Composable
fun PanicScreen(state: TasksUiState, onEdit: (Long) -> Unit, onTasks: () -> Unit, onRetry: () -> Unit) {
    val attention = state.rankedPending().filter { state.risks.getValue(it.id).needsAttention }
    ScreenList {
        item { ScreenHeading("PANIC MODE", "Focus on what matters right now.") }
        item { Text("Rule-based urgency from your saved deadlines and work estimates.", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant) }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { Text("${state.immediateCount} tasks need immediate attention", style = MaterialTheme.typography.titleMedium) }
                item { Text("${state.attentionCount} need planning or action • ${state.overdueCount} overdue", style = MaterialTheme.typography.bodyMedium) }
                if (attention.isEmpty()) item {
                    Panel {
                        Text("You're under control.", style = MaterialTheme.typography.titleLarge)
                        Text("No deadlines currently need urgent attention.")
                        TextButton(onClick = onTasks) { Text("View all tasks") }
                    }
                }
                itemsIndexed(attention, key = { _, task -> task.id }) { index, task ->
                    Panel {
                        Text("#${index + 1} • ${task.subject}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(task.title, style = MaterialTheme.typography.titleLarge)
                        Text("Due ${formatDeadline(task.dueDateMillis)}")
                        Text("Estimated work: ${formatWorkMinutes(task.estimatedMinutes)} • ${task.priority.label} priority")
                        RiskSummary(state.risks.getValue(task.id), expanded = true)
                        OutlinedButton(onClick = { onEdit(task.id) }, modifier = Modifier.fillMaxWidth()) { Text("View / edit task") }
                    }
                }
                item {
                    Text("Scores are urgency indicators, not failure probabilities. Estimates are treated as remaining work. Available time includes sleep and classes; update your estimates as you work.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
