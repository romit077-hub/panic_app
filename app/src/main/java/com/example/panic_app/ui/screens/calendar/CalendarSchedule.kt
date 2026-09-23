package com.example.panic_app.ui.screens.calendar

import com.example.panic_app.data.local.TaskEntity
import java.time.LocalDate

// Keep a restored selection inside the visible seven-day strip after midnight/resume.
internal fun selectedCalendarDay(selectedEpochDay: Long, today: LocalDate): LocalDate =
    LocalDate.ofEpochDay(selectedEpochDay.coerceIn(today.toEpochDay(), today.plusDays(6).toEpochDay()))

internal fun upcomingCalendarTasks(tasks: List<TaskEntity>, nowMillis: Long): List<TaskEntity> =
    tasks.filter { !it.isCompleted && it.dueDateMillis >= nowMillis }
        .sortedWith(compareBy<TaskEntity> { it.dueDateMillis }.thenBy { it.id })
