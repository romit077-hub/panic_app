package com.example.panic_app.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.model.DemoData
import com.example.panic_app.model.RiskLevel
import com.example.panic_app.ui.components.*
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen() {
    val completion = DemoData.completed.size.toFloat() / DemoData.tasks.size
    ScreenList {
        item { ScreenHeading("Your momentum", "Small steps add up to real progress.") }
        item { DemoNotice("DEMO PREVIEW • Statistics from sample tasks") }
        item {
            Panel {
                Text("Completion rate", style = MaterialTheme.typography.titleMedium)
                Text("${(completion * 100).roundToInt()}%", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(progress = { completion }, modifier = Modifier.fillMaxWidth())
                Text("${DemoData.completed.size} of ${DemoData.tasks.size} sample tasks completed")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Completed", DemoData.completed.size.toString(), Modifier.weight(1f))
                StatCard("Overdue", DemoData.overdue.size.toString(), Modifier.weight(1f))
            }
        }
        item { SectionHeader("Pending workload") }
        item {
            Panel {
                RiskLevel.entries.forEach { risk ->
                    val count = DemoData.pending.count { it.risk == risk }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        RiskBadge(risk); Text("$count tasks")
                    }
                    LinearProgressIndicator(progress = { count.toFloat() / DemoData.pending.size }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item { SectionHeader("By subject") }
        item {
            Panel {
                DemoData.pending.groupingBy { it.subject }.eachCount().forEach { (subject, count) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(subject); Text("$count pending") }
                }
            }
        }
    }
}
