package com.example.panic_app.ui.screens.planner

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.panic_app.domain.planner.*
import com.example.panic_app.ui.components.*
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlannerSettingsScreen(state: PlannerUiState, onSave: (PlannerConfig) -> Unit, onClose: () -> Unit) {
    val config = state.config
    var validation by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    fun changeWindow(index: Int, start: Int, end: Int) {
        if (start >= end) { validation = "End time must be after start time. Split overnight study across two days."; return }
        validation = null
        onSave(config.copy(availability = config.availability.mapIndexed { i, w -> if (i == index) w.copy(startMinute = start, endMinute = end) else w }))
    }
    ScreenList {
        item { SectionHeader("Planner settings", "Done", onClose) }
        item { Text("Changes save on this device. Overlapping windows are merged when planning. Estimates should describe work still remaining.") }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        validation?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (state.saving) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        item { Panel {
            Choice("Study session", config.sessionDurationMinutes, listOf(25, 30, 45, 60, 90), !state.saving) { onSave(config.copy(sessionDurationMinutes = it)) }
            Choice("Break", config.breakDurationMinutes, listOf(5, 10, 15, 20), !state.saving) { onSave(config.copy(breakDurationMinutes = it)) }
            Choice("Daily maximum", config.maxStudyMinutesPerDay, listOf(120, 180, 240, 300, 360), !state.saving) { onSave(config.copy(maxStudyMinutesPerDay = it)) }
            Choice("Planning horizon", config.horizonDays, listOf(3, 7, 14), !state.saving, "days") { onSave(config.copy(horizonDays = it)) }
        } }
        item { SectionHeader("Weekly availability") }
        DayOfWeek.entries.forEach { day ->
            item(key = day.name) {
                val windows = config.availability.withIndex().filter { it.value.day == day }
                Panel {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(day.getDisplayName(TextStyle.FULL, Locale.getDefault()), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Switch(modifier = Modifier.semantics { contentDescription = "Study on ${day.getDisplayName(TextStyle.FULL, Locale.getDefault())}" }, checked = windows.isNotEmpty(), enabled = !state.saving, onCheckedChange = { enabled ->
                            onSave(config.copy(availability = if (enabled) config.availability + AvailabilityWindow(day, 1080, 1320)
                                else config.availability.filterNot { it.day == day }))
                        })
                    }
                    if (windows.isEmpty()) Text("No availability")
                    windows.forEach { (index, window) ->
                        Column {
                            OutlinedButton(enabled = !state.saving, onClick = {
                                TimePickerDialog(context, { _, h, m -> changeWindow(index, h * 60 + m, window.endMinute) },
                                    window.startMinute / 60, window.startMinute % 60, android.text.format.DateFormat.is24HourFormat(context)).show()
                            }, modifier = Modifier.fillMaxWidth()) { Text("Start · ${windowTime(window.startMinute)}") }
                            OutlinedButton(enabled = !state.saving, onClick = {
                                TimePickerDialog(context, { _, h, m -> changeWindow(index, window.startMinute, if (h == 0 && m == 0) 1440 else h * 60 + m) },
                                    (window.endMinute / 60) % 24, window.endMinute % 60, android.text.format.DateFormat.is24HourFormat(context)).show()
                            }, modifier = Modifier.fillMaxWidth()) { Text("End · ${windowTime(window.endMinute)}") }
                            TextButton(enabled = !state.saving, onClick = { onSave(config.copy(availability = config.availability.filterIndexed { i, _ -> i != index })) }) { Text("Remove window") }
                        }
                    }
                    TextButton(enabled = !state.saving, onClick = { onSave(config.copy(availability = config.availability + AvailabilityWindow(day, 1080, 1320))) }) { Text("+ Add study window") }
                }
            }
        }
    }
}

@Composable
private fun Choice(label: String, value: Int, choices: List<Int>, enabled: Boolean, unit: String = "min", onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text("$label: $value $unit") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (choices + value).distinct().sorted().forEach { option ->
                DropdownMenuItem(text = { Text("$option $unit") }, onClick = { expanded = false; onSelect(option) })
            }
        }
    }
}
