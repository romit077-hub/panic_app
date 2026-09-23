package com.example.panic_app.ui.screens.tasks

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test

class TaskRiskStateTest {
    private val now = 1_800_000_000_000L
    private fun task(id: Long, minutes: Int) = TaskEntity(id, "Task $id", subject = "CN",
        dueDateMillis = now + minutes * 60_000L, estimatedMinutes = 1,
        priority = TaskPriority.LOW, createdAt = now)

    @Test fun completingAndDeletingTasksRemoveTheirAttentionCounts() {
        val urgent = task(1, 30)
        val initial = TasksUiState().withRisk(listOf(urgent, task(2, 20000)), now)
        assertEquals(1, initial.criticalCount); assertEquals(1, initial.attentionCount)
        val completed = initial.withRisk(listOf(urgent.copy(isCompleted = true), task(2, 20000)), now)
        assertEquals(0, completed.attentionCount); assertEquals(1, completed.completedCount)
        assertEquals(listOf(2L), completed.rankedPendingIds)
        val deleted = completed.withRisk(emptyList(), now)
        assertTrue(deleted.risks.isEmpty()); assertEquals(0, deleted.pendingCount)
    }
    @Test fun advancingTimeUpdatesRiskWithoutChangingRoomTasks() {
        val input = listOf(task(1, 3000))
        val initial = TasksUiState().withRisk(input, now)
        val later = initial.withRisk(input, now + 2990 * 60_000L)
        assertEquals(0, initial.attentionCount); assertEquals(1, later.criticalCount)
        assertEquals(input, later.tasks)
    }
    @Test fun editingEstimateAndDeadlineUpdatesRankingAndReason() {
        val first = task(1, 600)
        val initial = TasksUiState().withRisk(listOf(first, task(2, 120)), now)
        assertEquals(2L, initial.rankedPendingIds.first())
        val updated = initial.withRisk(listOf(first.copy(estimatedMinutes = 601), task(2, 120)), now)
        assertEquals(1L, updated.rankedPendingIds.first())
        assertTrue(updated.risks.getValue(1).reason.contains("fills or exceeds"))
        val rescheduled = updated.withRisk(listOf(first.copy(dueDateMillis = now + 30L * 24 * 60 * 60_000), task(2, 120)), now)
        assertFalse(rescheduled.risks.getValue(1).needsAttention)
    }
}
