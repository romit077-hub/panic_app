package com.example.panic_app.ui.screens.panic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panic_app.model.DemoData
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.theme.LocalPanicDarkTheme
import com.example.panic_app.ui.theme.riskColors
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun PanicScreen() {
    ScreenList {
        item { ScreenHeading("PANIC MODE", "Focus on what matters right now.") }
        item { DemoNotice("DEMO PREVIEW • Illustrative scores, no risk engine") }
        item { Text("${DemoData.urgent.size} priorities • ${DemoData.overdue.size} overdue", style = MaterialTheme.typography.titleMedium) }
        itemsIndexed(DemoData.urgent, key = { _, task -> task.id }) { index, task ->
            val colors = riskColors(task.risk, LocalPanicDarkTheme.current)
            Panel {
                Text("PRIORITY ${index + 1}", style = MaterialTheme.typography.labelLarge, color = colors.foreground)
                Text(task.title, style = MaterialTheme.typography.titleLarge)
                val minutes = Duration.between(LocalDateTime.now(), task.due).toMinutes()
                Text(if (minutes < 0) "Overdue • ${task.deadlineLabel}" else "Due in about ${(minutes + 59) / 60} hours • ${task.deadlineLabel}")
                RiskBadge(task.risk)
                Text("Risk: ${task.score} / 100", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { task.score / 100f }, modifier = Modifier.fillMaxWidth(), color = colors.foreground, trackColor = colors.background)
                Text("Recommended action", style = MaterialTheme.typography.labelLarge)
                Text(task.action)
            }
        }
    }
}
