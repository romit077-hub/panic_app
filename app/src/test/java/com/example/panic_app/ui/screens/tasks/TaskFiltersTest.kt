package com.example.panic_app.ui.screens.tasks

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TaskFiltersTest {
    private val now = Instant.parse("2026-09-24T18:00:00Z").toEpochMilli()
    private val zone = ZoneId.of("UTC")
    private fun task(id: Long, due: String, complete: Boolean = false) = TaskEntity(id, "Task $id", subject = "CN",
        dueDateMillis = Instant.parse(due).toEpochMilli(), estimatedMinutes = 30, priority = TaskPriority.HIGH,
        isCompleted = complete, createdAt = now)
    private val tasks = listOf(task(1, "2026-09-24T17:00:00Z"), task(2, "2026-09-25T10:00:00Z"),
        task(3, "2026-09-24T20:00:00Z", true), task(4, "2026-09-24T19:00:00Z"))
    private fun state() = TasksUiState().withRisk(tasks, now, zone)
    @Test fun todayIncludesEarlierTodayOverdueButNotCompleted() {
        assertEquals(setOf(1L, 4L), filterTasks(state(), TaskFilter.Today, now, zone).map { it.id }.toSet())
    }
    @Test fun upcomingMeansFutureDates() {
        assertEquals(listOf(2L), filterTasks(state(), TaskFilter.Upcoming, now, zone).map { it.id })
    }
    @Test fun criticalReusesExistingRiskAndOrder() {
        val state = state()
        val result = filterTasks(state, TaskFilter.Critical, now, zone)
        assertEquals(listOf(1L, 4L), result.map { it.id })
        assertTrue(result.all { state.risks.getValue(it.id).score >= 81 })
    }
    @Test fun allKeepsEachTaskOnceAndCompletedFilterIsExplicit() {
        assertEquals(4, filterTasks(state(), TaskFilter.All, now, zone).map { it.id }.distinct().size)
        assertEquals(listOf(3L), filterTasks(state(), TaskFilter.Completed, now, zone).map { it.id })
        assertEquals(state().rankedPendingIds, filterTasks(state(), TaskFilter.Pending, now, zone).map { it.id })
    }
    @Test fun todayUsesDeviceZoneAtMidnightBoundary() {
        val input = listOf(task(5, "2026-09-24T23:30:00Z"))
        val state = TasksUiState().withRisk(input, now, zone)
        assertTrue(filterTasks(state, TaskFilter.Today, now, ZoneId.of("Asia/Kolkata")).isEmpty())
        assertEquals(1, filterTasks(state, TaskFilter.Today, now, zone).size)
    }
    @Test fun emptyStateIsSafeForEveryFilter() {
        TaskFilter.entries.forEach { assertTrue(filterTasks(TasksUiState(), it, now, zone).isEmpty()) }
    }
}
