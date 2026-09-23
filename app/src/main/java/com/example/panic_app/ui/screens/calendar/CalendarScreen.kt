package com.example.panic_app.ui.screens.calendar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.ui.screens.tasks.TasksUiState
import java.time.Instant
import java.time.ZoneId
import com.example.panic_app.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(state: TasksUiState, onEdit: (Long) -> Unit, onRetry: () -> Unit) {
    val today = LocalDate.now()
    var selectedDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedDay)
    val pending = state.tasks.filterNot { it.isCompleted }
    fun localDate(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    val tasks = pending.filter { localDate(it.dueDateMillis) == selected }.sortedBy { it.dueDateMillis }
    ScreenList {
        item { ScreenHeading(today.format(DateTimeFormatter.ofPattern("MMMM yyyy")), "Your next seven days, at a glance.") }

        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0L..6L).forEach { offset ->
                    val day = today.plusDays(offset)
                    FilterChip(selected = day == selected, onClick = { selectedDay = day.toEpochDay() }, label = {
                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(day.format(DateTimeFormatter.ofPattern("EEE")))
                            Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge)
                        }
                    })
                }
            }
        }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { SectionHeader(selected.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))) }
                if (tasks.isEmpty()) item { Panel { Text("No pending deadlines on this date.") } }
                items(tasks, key = { it.id }) { task ->
                    DeadlineCard(task, state.risks.getValue(task.id), onEdit = { onEdit(task.id) })
                }
                item { SectionHeader("Upcoming deadlines") }
                val upcoming = pending.filter { !localDate(it.dueDateMillis).isBefore(today) }.sortedBy { it.dueDateMillis }
                if (upcoming.isEmpty()) item { Panel { Text("No upcoming deadlines.") } }
                items(upcoming, key = { "upcoming-${it.id}" }) { task ->
                    DeadlineCard(task, state.risks.getValue(task.id), onEdit = { onEdit(task.id) })
                }
            }
        }
    }
}
