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
import com.example.panic_app.model.ThemePreference

@Composable
fun SettingsScreen(theme: ThemePreference, notifications: Boolean, panicAlerts: Boolean, intensity: Int,
    onTheme: (ThemePreference) -> Unit, onNotifications: (Boolean) -> Unit, onPanicAlerts: (Boolean) -> Unit, onIntensity: (Int) -> Unit,
    status: String, feedback: String?, busy: Boolean, canRequestPermission: Boolean,
    onPermission: () -> Unit, onSystemSettings: () -> Unit, debug: Boolean, onCheckNow: () -> Unit) {
    ScreenList {
        item { ScreenHeading("Make it yours", "Choose how PANIC feels for you.") }
        item { Panel {
            Text(if (notifications) "Reminders requested" else "Reminders off", style = MaterialTheme.typography.titleMedium)
            Text(status)
            Text("Notifications help you act before deadlines become critical. Android permission and channel settings also control delivery.", style = MaterialTheme.typography.bodySmall)
            if (canRequestPermission) Button(onClick = onPermission, enabled = !busy) { Text("Allow notifications") }
            TextButton(onClick = onSystemSettings) { Text("Open Android notification settings") }
            feedback?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        } }
        item { SectionHeader("Appearance") }
        item { Panel {
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Text("Saved on this device. System follows your Android appearance setting.")
            ThemePreference.entries.forEach { option ->
                OutlinedButton(onClick = { onTheme(option) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (theme == option) "✓ ${option.label}" else option.label)
                }
            }
        } }
        item { SectionHeader("Reminder preferences") }
        item {
            Panel {
                Text("Preferences are saved on this device. Background checks are approximate and may be delayed by Android.", style = MaterialTheme.typography.bodyMedium)
                SettingToggle("Notifications", "Allow risk-based deadline reminders", notifications, onNotifications, !busy)
                HorizontalDivider()
                SettingToggle("Panic alerts", "Allow HIGH, CRITICAL and overdue alerts", panicAlerts, onPanicAlerts, !busy)
            }
        }
        item {
            Panel {
                Text("Reminder intensity", style = MaterialTheme.typography.titleMedium)
                Text("Gentle: high risk and above. Balanced: warning and above. Aggressive: shorter cooldowns, still throttled.", style = MaterialTheme.typography.bodySmall)
                listOf("Gentle", "Balanced", "Aggressive").forEachIndexed { index, title ->
                    OutlinedButton(onClick = { onIntensity(index) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        Text(if (intensity == index) "✓ $title" else title)
                    }
                }
            }
        }
        if (debug) item { Panel {
            Text("Developer tools • Debug build", style = MaterialTheme.typography.titleMedium)
            Text("Runs the real check immediately. Permissions, saved preferences and cooldowns still apply. At most one reminder per check.")
            Button(onClick = onCheckNow, enabled = !busy) { Text(if (busy) "Working…" else "Run deadline check now") }
        } }
        item { Panel { Text("PANIC — Deadline Enforcer", style = MaterialTheme.typography.titleMedium); Text("College demo • Offline task management\nTasks are stored with Room. Risk-based reminders use the same engine as PANIC Mode.", style = MaterialTheme.typography.bodyMedium) } }
    }
}

@Composable
private fun SettingToggle(title: String, description: String, value: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(enabled = enabled, checked = value, onCheckedChange = onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}
