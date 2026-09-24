package com.example.panic_app.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import com.example.panic_app.ui.screens.planner.studyDuration
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
    val zone = ZoneId.systemDefault()
    val today = if (state.calculatedAtMillis == 0L) LocalDate.now(zone)
        else Instant.ofEpochMilli(state.calculatedAtMillis).atZone(zone).toLocalDate()
    var selectedDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val selected = selectedCalendarDay(selectedDay, today)
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
                            val dayTasks = pending.filter { localDate(it.dueDateMillis) == day }
                            Text("${dayTasks.size} due", style = MaterialTheme.typography.labelSmall)
                            val risk = dayTasks.mapNotNull { state.risks[it.id] }.maxByOrNull { it.score }
                            if (risk != null) RiskBadge(risk.level)
                        }
                    })
                }
            }
        }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item {
                    AnimatedContent(targetState = selected, label = "Selected date") { date ->
                        Column {
                            SectionHeader(date.format(DateTimeFormatter.ofPattern("EEEE, d MMM")))
                            val due = pending.filter { localDate(it.dueDateMillis) == date }
                            Text("${due.size} tasks • ${studyDuration(due.sumOf { it.estimatedMinutes.coerceAtLeast(0).toLong() })} estimated work")
                        }
                    }
                }
                if (tasks.isEmpty()) item { Panel { Text("No pending deadlines on this date.") } }
                items(tasks, key = { it.id }) { task ->
                    DeadlineCard(task, state.risks.getValue(task.id), onEdit = { onEdit(task.id) }, modifier = Modifier.animateItem())
                }
                item { SectionHeader("Upcoming deadlines") }
                val upcoming = upcomingCalendarTasks(state.tasks, state.calculatedAtMillis)
                if (upcoming.isEmpty()) item { Panel { Text("No upcoming deadlines.") } }
                items(upcoming, key = { "upcoming-${it.id}" }) { task ->
                    DeadlineCard(task, state.risks.getValue(task.id), onEdit = { onEdit(task.id) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}
