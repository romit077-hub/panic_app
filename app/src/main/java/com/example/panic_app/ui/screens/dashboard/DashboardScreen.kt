package com.example.panic_app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.panic_app.model.DemoData
import com.example.panic_app.model.RiskLevel
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.theme.Panic_appTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(onTasks: () -> Unit, onPanic: () -> Unit, onAnalytics: () -> Unit, onSettings: () -> Unit, onAdd: () -> Unit) {
    ScreenList {
        item {
            val greeting = when (LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
            ScreenHeading(greeting, "Let's keep those deadlines under control.")
        }
        item { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { DemoNotice() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Pending", DemoData.pending.size.toString(), Modifier.weight(1f))
                StatCard("Completed", DemoData.completed.size.toString(), Modifier.weight(1f))
                StatCard("Critical", DemoData.pending.count { it.risk == RiskLevel.CRITICAL }.toString(), Modifier.weight(1f))
            }
        }
        item { SectionHeader("Next deadline", "All tasks", onTasks) }
        item { DeadlineCard(DemoData.nextDeadline) }
        item { SectionHeader("Today's focus") }
        item { TaskCard(DemoData.tasks.first { it.id == 4 }) }
        item { TaskCard(DemoData.tasks.first { it.id == 3 }) }
        item {
            Panel {
                Text("PANIC MODE", style = MaterialTheme.typography.titleLarge)
                Text("${DemoData.urgent.size} deadlines need attention. Start with the most urgent.")
                Button(onClick = onPanic, modifier = Modifier.fillMaxWidth()) { Text("Enter Panic Mode") }
            }
        }
        item {
            FilledTonalButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Task")
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

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() { Panic_appTheme { DashboardScreen({}, {}, {}, {}, {}) } }
