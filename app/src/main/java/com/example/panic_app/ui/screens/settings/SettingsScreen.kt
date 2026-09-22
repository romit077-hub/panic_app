package com.example.panic_app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.panic_app.ui.components.*

@Composable
fun SettingsScreen(darkTheme: Boolean, notifications: Boolean, panicAlerts: Boolean, intensity: Int,
    onDarkTheme: (Boolean) -> Unit, onNotifications: (Boolean) -> Unit, onPanicAlerts: (Boolean) -> Unit, onIntensity: (Int) -> Unit) {
    ScreenList {
        item { ScreenHeading("Make it yours", "Choose how PANIC feels for you.") }
        item { DemoNotice("PREVIEW SETTINGS • Not saved permanently") }
        item { SectionHeader("Appearance") }
        item { Panel { SettingToggle("Dark mode", "Applies across every screen immediately.", darkTheme, onDarkTheme) } }
        item { SectionHeader("Reminder preferences") }
        item {
            Panel {
                Text("Preview only. These controls do not schedule reminders or send notifications.", style = MaterialTheme.typography.bodyMedium)
                SettingToggle("Notifications", "Example reminder preference", notifications, onNotifications)
                HorizontalDivider()
                SettingToggle("Panic alerts", "Example urgent alert preference", panicAlerts, onPanicAlerts)
            }
        }
        item {
            Panel {
                Text("Reminder intensity", style = MaterialTheme.typography.titleMedium)
                listOf("Gentle", "Balanced", "Strong").forEachIndexed { index, title ->
                    OutlinedButton(onClick = { onIntensity(index) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (intensity == index) "✓ $title" else title)
                    }
                }
            }
        }
        item { Panel { Text("PANIC — Deadline Enforcer", style = MaterialTheme.typography.titleMedium); Text("College demo • Offline task management\nTasks are stored with Room. Reminder controls are previews only.", style = MaterialTheme.typography.bodyMedium) } }
    }
}

@Composable
private fun SettingToggle(title: String, description: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = value, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}
