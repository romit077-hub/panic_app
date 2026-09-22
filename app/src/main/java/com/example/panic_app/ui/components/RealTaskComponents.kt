package com.example.panic_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.util.formatDeadline

@Composable
fun TaskCard(task: TaskEntity, busy: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, onToggle: () -> Unit) {
    Panel {
        Text(task.title, style = MaterialTheme.typography.titleMedium,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
        Text("${task.subject} • ${formatDeadline(task.dueDateMillis)}", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (task.description.isNotBlank()) Text(task.description, style = MaterialTheme.typography.bodyMedium)
        Text("${task.estimatedMinutes} min estimate • ${task.priority.label} priority", style = MaterialTheme.typography.bodySmall)
        Text(if (task.isCompleted) "Completed" else "Pending", color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge)
        FilledTonalButton(onClick = onToggle, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "Updating…" else if (task.isCompleted) "Mark incomplete" else "Mark complete")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onEdit, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Edit") }
            TextButton(onClick = onDelete, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun DeadlineCard(task: TaskEntity, onEdit: () -> Unit) {
    Panel {
        Text(task.title, style = MaterialTheme.typography.titleLarge)
        Text("${task.subject} • ${task.priority.label} priority", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatDeadline(task.dueDateMillis), color = MaterialTheme.colorScheme.primary)
        Text("${task.estimatedMinutes} minutes estimated work", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onEdit) { Text("View / edit task") }
    }
}

@Composable
fun TaskLoading() {
    Panel { CircularProgressIndicator(); Text("Loading your deadlines…") }
}

@Composable
fun TaskError(message: String, onRetry: () -> Unit) {
    Panel {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyTasks(title: String = "No deadlines yet", subtitle: String = "Add your first task and PANIC will help you stay ahead.", onAdd: () -> Unit) {
    Panel {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle)
        Button(onClick = onAdd) { Text("+ Add Task") }
    }
}
