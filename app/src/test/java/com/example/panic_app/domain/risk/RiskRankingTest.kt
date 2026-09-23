package com.example.panic_app.domain.risk

import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test

class RiskRankingTest {
    private val now = 1_800_000_000_000L
    private fun assess(id: Long, minutes: Long, estimate: Int = 1, completed: Boolean = false): AssessedTask {
        val task = RiskTask(id, now + minutes * MILLIS_PER_MINUTE, estimate, TaskPriority.LOW, completed)
        return AssessedTask(task, RiskCalculator.calculate(task, now))
    }
    @Test fun overduePrecedesAnEquallyCriticalTaskDueNow() {
        assertEquals(listOf(2L, 1L), RiskRanking.pending(listOf(assess(1, 0), assess(2, -1))).map { it.task.id })
    }
    @Test fun higherRiskPrecedesNearerDeadline() {
        // A far-away task needing more work than wall-clock capacity outranks a small imminent task.
        val nearer = assess(1, 120)
        val heavier = assess(2, 20000, 21000)
        assertEquals(listOf(2L, 1L), RiskRanking.pending(listOf(nearer, heavier)).map { it.task.id })
    }
    @Test fun equalScoresUseDeadlineThenStableId() {
        val inputs = listOf(assess(9, 15000), assess(3, 12000), assess(2, 12000))
        assertEquals(listOf(2L, 3L, 9L), RiskRanking.pending(inputs).map { it.task.id })
        assertEquals(listOf(9L, 3L, 2L), inputs.map { it.task.id }) // input not mutated
    }
    @Test fun completedTasksNeverEnterRanking() {
        assertTrue(RiskRanking.pending(listOf(assess(1, -500, completed = true))).isEmpty())
    }
}
