package com.example.panic_app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.model.DemoData
import com.example.panic_app.ui.components.*

@Composable
fun TasksScreen() {
    var selected by rememberSaveable { mutableStateOf("Pending") }
    val visible = when (selected) { "Completed" -> DemoData.completed; "All" -> DemoData.tasks; else -> DemoData.pending }
    ScreenList {
        item { ScreenHeading("Your tasks", "A clear view of what needs your attention.") }
        item { DemoNotice("DEMO PREVIEW • Read-only sample tasks") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pending", "Completed", "All").forEach { label ->
                    FilterChip(selected = selected == label, onClick = { selected = label }, label = { Text(label) })
                }
            }
        }
        item { Text("${visible.size} tasks", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(visible, key = { it.id }) { TaskCard(it) }
    }
}
