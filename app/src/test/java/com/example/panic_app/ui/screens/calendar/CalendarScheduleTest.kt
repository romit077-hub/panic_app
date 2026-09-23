package com.example.panic_app.ui.screens.calendar

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.model.TaskPriority
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class CalendarScheduleTest {
    private val now = Instant.parse("2026-09-24T12:00:00Z").toEpochMilli()
    private fun task(id: Long, due: Long, complete: Boolean = false) =
        TaskEntity(id, "Task", subject = "CN", dueDateMillis = due, estimatedMinutes = 60,
            priority = TaskPriority.LOW, isCompleted = complete, createdAt = now)
    @Test fun upcomingExcludesEarlierTodayAndCompletedTasks() {
        val result = upcomingCalendarTasks(listOf(task(1, now - 1), task(2, now),
            task(3, now + 1), task(4, now + 1, complete = true)), now)
        assertEquals(listOf(2L, 3L), result.map { it.id })
    }
    @Test fun upcomingSortsDeadlineThenId() {
        val result = upcomingCalendarTasks(listOf(task(3, now + 10), task(2, now), task(1, now)), now)
        assertEquals(listOf(1L, 2L, 3L), result.map { it.id })
    }
    @Test fun midnightMovesExpiredSelectionIntoVisibleStrip() {
        val today = LocalDate.of(2026, 9, 24)
        assertEquals(today, selectedCalendarDay(today.minusDays(1).toEpochDay(), today))
        assertEquals(today.plusDays(2), selectedCalendarDay(today.plusDays(2).toEpochDay(), today))
        assertEquals(today.plusDays(6), selectedCalendarDay(today.plusDays(20).toEpochDay(), today))
    }
}
