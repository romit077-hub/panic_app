package com.example.panic_app.ui.screens.addedittask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.panic_app.model.TaskDraft
import com.example.panic_app.model.TaskPriority
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.theme.LocalPanicDarkTheme
import com.example.panic_app.util.deadlineTimestamp
import com.example.panic_app.util.formatDeadline
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun TaskEditorScreen(state: TaskEditorState, editing: Boolean, onChange: (TaskDraft) -> Unit,
    onDeadline: (Long) -> Unit, onDateError: () -> Unit, onSave: () -> Unit, onCancel: () -> Unit, onRetry: () -> Unit) {
    val draft = state.draft
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    // Finish the write before navigating away; otherwise the destination VM is cancelled.
    BackHandler(enabled = state.saving) { }
    ScreenList {
        item { ScreenHeading(if (editing) "Edit deadline" else "Add a deadline", "Make room for what matters.") }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { EditorField("Title *", draft.title, { onChange(draft.copy(title = it)) }, state.errors["title"], !state.saving) }
                item { EditorField("Subject *", draft.subject, { onChange(draft.copy(subject = it)) }, state.errors["subject"], !state.saving) }
                item { EditorField("Description", draft.description, { onChange(draft.copy(description = it)) }, state.errors["description"], !state.saving, multiline = true) }
                item {
                    Panel {
                        Text("Deadline *", style = MaterialTheme.typography.titleMedium)
                        Text(formatDeadline(draft.dueDateMillis))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { picker = "date" }, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text("Choose date") }
                            OutlinedButton(onClick = { picker = "time" }, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text("Choose time") }
                        }
                        Text("Local time: ${ZoneId.systemDefault().id}. Past deadlines are allowed for tracking overdue work.", style = MaterialTheme.typography.bodySmall)
                        state.errors["deadline"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
                item { EditorField("Estimated minutes *", draft.estimatedMinutes, { onChange(draft.copy(estimatedMinutes = it)) }, state.errors["estimate"], !state.saving, numeric = true) }
                item {
                    Panel {
                        Text("Priority", style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskPriority.entries.forEach { priority ->
                                FilterChip(selected = draft.priority == priority, enabled = !state.saving,
                                    onClick = { onChange(draft.copy(priority = priority)) }, label = { Text(priority.label) })
                            }
                        }
                    }
                }
                state.saveError?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                item {
                    Button(onClick = onSave, enabled = !state.saving && !state.saved, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.saving) "Saving…" else if (editing) "Save changes" else "Save Task")
                    }
                }
            }
        }
        item { OutlinedButton(onClick = onCancel, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }
    }
    if (picker != null) {
        val context = LocalContext.current
        val dialogTheme = if (LocalPanicDarkTheme.current) android.R.style.Theme_Material_Dialog_Alert
            else android.R.style.Theme_Material_Light_Dialog_Alert
        DisposableEffect(picker, dialogTheme) {
            val current = Instant.ofEpochMilli(draft.dueDateMillis).atZone(ZoneId.systemDefault())
            fun choose(date: LocalDate, time: LocalTime) {
                val timestamp = deadlineTimestamp(date, time)
                if (timestamp == null) onDateError() else onDeadline(timestamp)
            }
            val dialog = if (picker == "date") {
                DatePickerDialog(context, dialogTheme, { _, year, month, day -> choose(LocalDate.of(year, month + 1, day), current.toLocalTime()) },
                    current.year, current.monthValue - 1, current.dayOfMonth).apply {
                    datePicker.minDate = LocalDate.of(1970, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    datePicker.maxDate = LocalDate.of(2100, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            } else {
                TimePickerDialog(context, dialogTheme, { _, hour, minute -> choose(current.toLocalDate(), LocalTime.of(hour, minute)) },
                    current.hour, current.minute, DateFormat.is24HourFormat(context))
            }
            dialog.setOnDismissListener { picker = null }
            dialog.show()
            onDispose { dialog.setOnDismissListener(null); dialog.dismiss() }
        }
    }
}

@Composable
private fun EditorField(label: String, value: String, onChange: (String) -> Unit,
    error: String?, enabled: Boolean, multiline: Boolean = false, numeric: Boolean = false) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = !multiline,
        minLines = if (multiline) 3 else 1, maxLines = if (multiline) 6 else 1, isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text))
}
