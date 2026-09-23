package com.example.panic_app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.ui.components.*

@Composable
fun TasksScreen(state: TasksUiState, onAdd: () -> Unit, onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit, onToggle: (Long) -> Unit, onRetry: () -> Unit, onDismissError: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf("Pending") }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val deleting = state.tasks.firstOrNull { it.id == deleteId }
    val visible = when (selected) {
        "Completed" -> state.tasks.filter { it.isCompleted }
        "All" -> state.rankedPending() + state.tasks.filter { it.isCompleted }
        else -> state.rankedPending()
    }
    ScreenList {
        item { ScreenHeading("Your tasks", "Your deadlines, saved offline on this device.") }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("+ Add Task") } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pending", "Completed", "All").forEach { label ->
                    FilterChip(selected = selected == label, onClick = { selected = label }, label = { Text(label) })
                }
            }
        }
        state.actionError?.let { message -> item { Panel {
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onDismissError) { Text("Dismiss") }
        } } }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            state.tasks.isEmpty() -> item { EmptyTasks(onAdd = onAdd) }
            visible.isEmpty() -> item { EmptyTasks("No ${selected.lowercase()} tasks", "Choose another filter or add a deadline.", onAdd) }
            else -> {
                item { Text("${visible.size} tasks • Pending tasks ordered by urgency", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(visible, key = { it.id }) { task ->
                    TaskCard(task, state.risks.getValue(task.id), task.id in state.busyIds, onEdit = { onEdit(task.id) },
                        onDelete = { deleteId = task.id }, onToggle = { onToggle(task.id) })
                }
            }
        }
    }
    if (deleting != null) AlertDialog(onDismissRequest = { deleteId = null },
        title = { Text("Delete task?") },
        text = { Text("“${deleting.title}” will be permanently removed.") },
        dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } },
        confirmButton = { TextButton(onClick = { deleteId = null; onDelete(deleting.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error) } })
}
