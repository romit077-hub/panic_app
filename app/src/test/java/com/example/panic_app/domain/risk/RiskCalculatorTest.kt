package com.example.panic_app.domain.risk

import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test

class RiskCalculatorTest {
    private val now = 1_800_000_000_000L
    private fun task(minutesLeft: Long = 10 * 24 * 60L, estimate: Int = 60,
        priority: TaskPriority = TaskPriority.LOW, completed: Boolean = false) =
        RiskTask(1, now + minutesLeft * MILLIS_PER_MINUTE, estimate, priority, completed)
    private fun calculate(task: RiskTask) = RiskCalculator.calculate(task, now)

    @Test fun completedOverdueTaskIsInactive() {
        val result = calculate(task(-60, completed = true))
        assertEquals(0, result.score); assertEquals(RiskLevel.SAFE, result.level)
        assertFalse(result.isOverdue); assertFalse(result.isActive); assertFalse(result.needsAttention)
    }
    @Test fun overdueIncompleteTaskIsCritical() {
        val result = calculate(task(-1))
        assertEquals(100, result.score); assertTrue(result.isOverdue)
        assertEquals(RiskLevel.CRITICAL, result.level)
        assertTrue(result.recommendedAction.contains("reschedule"))
    }
    @Test fun exactlyNowIsCriticalButNotYetOverdue() {
        val result = calculate(task(0))
        assertEquals(100, result.score); assertFalse(result.isOverdue)
        assertEquals("Due now", formatRemainingTime(result.remainingMillis))
    }
    @Test fun distantLowPriorityTaskIsSafe() {
        val result = calculate(task())
        assertEquals(5, result.score); assertEquals(RiskLevel.SAFE, result.level)
    }
    @Test fun deadlineWithin24HoursHasSignificantPressure() {
        val result = calculate(task(20L * 60, 120))
        assertEquals(35, result.score); assertEquals(RiskLevel.WARNING, result.level)
    }
    @Test fun imminentLowPriorityTaskIsStillCritical() {
        val result = calculate(task(90, 1))
        assertEquals(85, result.score); assertEquals(RiskLevel.CRITICAL, result.level)
    }
    @Test fun almostFullWorkloadBecomesCriticalWithin10Hours() {
        val result = calculate(task(600, 540))
        assertEquals(85, result.score); assertEquals(RiskLevel.CRITICAL, result.level)
    }
    @Test fun exceedingCapacityIsCriticalEvenWithDistantDeadline() {
        val result = calculate(task(10 * 24 * 60L, 20_000))
        assertEquals(90, result.score); assertTrue(result.capacityFloorApplied)
        assertEquals(RiskLevel.CRITICAL, result.level)
    }
    @Test fun priorityAddsAtMost10Points() {
        val low = calculate(task(48L * 60, priority = TaskPriority.LOW))
        val medium = calculate(task(48L * 60, priority = TaskPriority.MEDIUM))
        val high = calculate(task(48L * 60, priority = TaskPriority.HIGH))
        assertEquals(low.score + 5, medium.score); assertEquals(low.score + 10, high.score)
    }
    @Test fun highPriorityNextMonthRemainsSafe() {
        val result = calculate(task(30L * 24 * 60, priority = TaskPriority.HIGH))
        assertEquals(15, result.score); assertEquals(RiskLevel.SAFE, result.level)
    }
    @Test fun excessiveInputsAreClampedWithoutIntegerOverflow() {
        val result = calculate(task(1, Int.MAX_VALUE, TaskPriority.HIGH))
        assertEquals(100, result.score)
    }
    @Test fun zeroAndNegativeEstimatesHaveExplicitFallback() {
        val zero = calculate(task(300, 0))
        val negative = calculate(task(300, Int.MIN_VALUE))
        assertEquals(calculate(task(300, 1)).score, zero.score)
        assertEquals(zero.score, negative.score)
        assertTrue(zero.reason.contains("using 1 minute"))
    }
    @Test fun scoreBoundariesAreCentralAndExact() {
        val cases = mapOf(0 to RiskLevel.SAFE, 30 to RiskLevel.SAFE, 31 to RiskLevel.WARNING,
            60 to RiskLevel.WARNING, 61 to RiskLevel.HIGH, 80 to RiskLevel.HIGH,
            81 to RiskLevel.CRITICAL, 100 to RiskLevel.CRITICAL)
        cases.forEach { (score, level) -> assertEquals(level, RiskLevel.fromScore(score)) }
    }
    @Test fun timeBandBoundariesUseTheMoreUrgentBandAtEquality() {
        val cases = mapOf(10081L to 5, 10080L to 10, 4321L to 10, 4320L to 20,
            1441L to 20, 1440L to 35, 721L to 35, 720L to 45, 361L to 45,
            360L to 60, 121L to 60, 120L to 85)
        cases.forEach { (minutes, points) -> assertEquals("$minutes minutes", points, calculate(task(minutes, 1)).timePoints) }
    }
    @Test fun workloadBandsAndCapacityEqualityAreCorrect() {
        val cases = mapOf(100 to 0, 101 to 5, 250 to 5, 251 to 15,
            500 to 15, 501 to 25, 750 to 25, 751 to 40, 999 to 40, 1000 to 50)
        cases.forEach { (estimate, points) -> assertEquals(points, calculate(task(1000, estimate)).workloadPoints) }
        assertEquals(90, calculate(task(1000, 1000)).score)
    }
    @Test fun oneMillisecondRemainingDoesNotRoundIntoOverdue() {
        val result = calculate(task().copy(dueDateMillis = now + 1))
        assertFalse(result.isOverdue); assertEquals(100, result.score)
        assertEquals("less than 1 minute remaining", formatRemainingTime(result.remainingMillis))
    }
    @Test fun timestampExtremesPreserveSignWithoutOverflow() {
        val distant = RiskCalculator.calculate(task().copy(dueDateMillis = Long.MAX_VALUE), Long.MIN_VALUE)
        val overdue = RiskCalculator.calculate(task().copy(dueDateMillis = Long.MIN_VALUE), Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, distant.remainingMillis); assertFalse(distant.isOverdue)
        assertEquals(Long.MIN_VALUE, overdue.remainingMillis); assertTrue(overdue.isOverdue)
        assertEquals(100, overdue.score)
    }
    @Test fun riskDoesNotDecreaseAsDeadlineApproaches() {
        val scores = listOf(20_000L, 10_000L, 4000L, 1440L, 720L, 360L, 120L, 1L, 0L, -1L)
            .map { calculate(task(it, 120)).score }
        assertTrue(scores.zipWithNext().all { (earlier, later) -> later >= earlier })
    }
    @Test fun riskDoesNotDecreaseAsRemainingWorkIncreases() {
        val scores = listOf(1, 60, 600, 1440, 2200, 2879, 2880, 6000, Int.MAX_VALUE)
            .map { calculate(task(2880, it)).score }
        assertTrue(scores.zipWithNext().all { (less, more) -> more >= less })
    }
    @Test fun allEdgeCasesStayWithinScoreRange() {
        for (minutes in listOf(-1L, 0L, 1L, 120L, 1440L, 100_000L))
            for (estimate in listOf(Int.MIN_VALUE, 0, 1, 60, 10_080, Int.MAX_VALUE))
                for (priority in TaskPriority.entries) {
                    val result = calculate(task(minutes, estimate, priority))
                    assertTrue(result.score in 0..100)
                    assertEquals(RiskLevel.fromScore(result.score), result.level)
                }
    }
    @Test fun sameInputAndTimeProduceExactlySameExplanation() {
        val task = task(540, 480)
        assertEquals(calculate(task), calculate(task))
        assertTrue(calculate(task).reason.contains("9 hours"))
        assertTrue(calculate(task).reason.contains("8 hours"))
    }
}
