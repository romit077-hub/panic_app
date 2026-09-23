package com.example.panic_app.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.ui.components.*

@Composable
fun AnalyticsScreen() {
    ScreenList {
        item { ScreenHeading("Your momentum", "Small steps add up to real progress.") }
        item { DemoNotice("DESIGN PREVIEW • Sample statistics, not your saved tasks") }
        item {
            Panel {
                Text("Example completion rate", style = MaterialTheme.typography.titleMedium)
                Text("72%", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(progress = { 0.72f }, modifier = Modifier.fillMaxWidth())
                Text("Illustration: 18 of 25 tasks completed")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Example completed", "18", Modifier.weight(1f))
                StatCard("Example streak", "4 days", Modifier.weight(1f))
            }
        }
        item { Panel { Text("Live analytics will be added separately. Your real task counts and urgency scores are available on Dashboard and in Panic Mode.") } }
    }
}
