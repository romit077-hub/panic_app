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
import com.example.panic_app.model.DemoData
import com.example.panic_app.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen() {
    val today = LocalDate.now()
    var selectedDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedDay)
    val tasks = DemoData.pending.filter { it.due.toLocalDate() == selected }.sortedBy { it.due }
    ScreenList {
        item { ScreenHeading(today.format(DateTimeFormatter.ofPattern("MMMM yyyy")), "Your next seven days, at a glance.") }
        item { DemoNotice("DESIGN PREVIEW • Sample schedule, not your saved tasks") }
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
        item { SectionHeader(selected.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))) }
        if (tasks.isEmpty()) item { Panel { Text("No sample deadlines on this date."); Text("Choose another day to explore your schedule.", style = MaterialTheme.typography.bodyMedium) } }
        items(tasks, key = { it.id }) { TaskCard(it) }
        item { SectionHeader("Upcoming deadlines") }
        items(DemoData.pending.filter { !it.due.toLocalDate().isBefore(today) }.sortedBy { it.due }, key = { "upcoming-${it.id}" }) { TaskCard(it) }
    }
}
