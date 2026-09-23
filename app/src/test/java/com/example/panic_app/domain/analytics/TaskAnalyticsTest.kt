package com.example.panic_app.domain.analytics

import com.example.panic_app.domain.risk.*
import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TaskAnalyticsTest {
    private val now = Instant.parse("2026-09-23T12:00:00Z").toEpochMilli()
    private val hour = 3_600_000L
    private val zone = ZoneId.of("UTC")
    private fun task(id: Long = 1, due: Long = now + hour, minutes: Int = 60,
        done: Boolean = false, priority: TaskPriority = TaskPriority.LOW) =
        AnalyticsTask(RiskTask(id, due, minutes, priority, done), "Task $id")
    private fun analyze(vararg tasks: AnalyticsTask) = TaskAnalytics.calculate(tasks.toList(), now, zone).summary

    @Test fun emptyListHasNoInventedPercentage() {
        val s = analyze()
        assertEquals(0, s.total); assertEquals(0, s.pending); assertNull(s.completionPercent)
        assertEquals(0f, s.completionFraction, 0f); assertNull(s.nearest)
        assertEquals(0L, s.pendingMinutes); assertEquals(0, s.riskCounts.values.sum())
        assertEquals(InsightKind.EMPTY, s.insights.single().kind)
    }
    @Test fun completionRateRoundsSensibly() {
        val s = analyze(task(1, done = true), task(2), task(3))
        assertEquals(33, s.completionPercent); assertEquals(1f / 3, s.completionFraction, 0.00001f)
    }
    @Test fun overdueUsesStrictTimestampComparison() {
        val s = analyze(task(1, due = now - 1), task(2, due = now), task(3, due = now + 1))
        assertEquals(1, s.overdue)
        assertEquals(2, s.next24Hours.count)
    }
    @Test fun completedOldTaskIsNotCurrentlyOverdue() {
        val s = analyze(task(due = now - hour, done = true))
        assertEquals(0, s.overdue); assertEquals(0, s.critical); assertNull(s.nearest)
    }
    @Test fun workloadIncludesOnlyPendingEstimates() {
        val s = analyze(task(1, minutes = 45), task(2, minutes = 90), task(3, minutes = 800, done = true))
        assertEquals(135L, s.pendingMinutes)
    }
    @Test fun twentyFourHourWindowIsInclusiveAndExcludesOverdue() {
        val s = analyze(task(1, due = now - 1, minutes = 90), task(2, due = now, minutes = 10),
            task(3, due = now + 24 * hour, minutes = 20), task(4, due = now + 24 * hour + 1, minutes = 100))
        assertEquals(WorkloadPeriod(2, 30), s.next24Hours)
    }
    @Test fun sevenDayWindowIsInclusiveAndExcludesCompleted() {
        val s = analyze(task(1, due = now + 168 * hour, minutes = 75),
            task(2, due = now + 168 * hour + 1), task(3, done = true))
        assertEquals(WorkloadPeriod(1, 75), s.next7Days)
    }
    @Test fun riskDistributionMatchesExistingCalculator() {
        val tasks = listOf(task(1, due = now + 30 * 24 * hour, minutes = 1),
            task(2, due = now + 24 * hour, minutes = 1),
            task(3, due = now + 6 * hour, minutes = 1, priority = TaskPriority.HIGH),
            task(4, due = now + hour, minutes = 1), task(5, done = true))
        val result = TaskAnalytics.calculate(tasks, now, zone)
        RiskLevel.entries.forEach { assertEquals(1, result.summary.riskCounts.getValue(it)) }
        tasks.forEach { assertEquals(RiskCalculator.calculate(it.task, now), result.risks.getValue(it.task.id)) }
    }
    @Test fun priorityDistributionExcludesCompleted() {
        val s = analyze(task(1), task(2, priority = TaskPriority.MEDIUM), task(3, priority = TaskPriority.HIGH),
            task(4, priority = TaskPriority.HIGH, done = true))
        TaskPriority.entries.forEach { assertEquals(1, s.priorityCounts.getValue(it)) }
    }
    @Test fun criticalInsightUsesRealRisk() {
        assertTrue(analyze(task()).insights.any { it.kind == InsightKind.CRITICAL })
    }
    @Test fun overdueInsightHasHighestPriority() {
        val s = analyze(task(1, due = now - hour), task(2), task(3, done = true))
        assertEquals(InsightKind.OVERDUE, s.insights.first().kind)
        assertTrue(s.insights.size <= 4)
    }
    @Test fun allCompletedGeneratesCaughtUpInsight() {
        val s = analyze(task(done = true))
        assertEquals(100, s.completionPercent); assertEquals(1f, s.completionFraction, 0f)
        assertTrue(s.insights.any { it.kind == InsightKind.ALL_CLEAR })
    }
    @Test fun percentagesRemainInBoundsAcrossAllCompletionCounts() {
        for (completed in 0..20) {
            val tasks = (0 until 20).map { task(it.toLong(), done = it < completed) }
            val s = TaskAnalytics.calculate(tasks, now, zone).summary
            assertTrue(s.completionPercent!! in 0..100)
            assertTrue(s.completionFraction in 0f..1f)
        }
    }
    @Test fun allPendingIsZeroPercent() { assertEquals(0, analyze(task()).completionPercent) }
    @Test fun largeWorkloadDoesNotOverflowInt() {
        assertEquals(4_294_967_294L, analyze(task(1, minutes = Int.MAX_VALUE), task(2, minutes = Int.MAX_VALUE)).pendingMinutes)
    }
    @Test fun zeroAndInvalidNegativeEstimatesCannotCreateNegativeWork() {
        val s = analyze(task(1, minutes = 0), task(2, minutes = -10))
        assertEquals(0L, s.pendingMinutes); assertEquals(0L, s.next24Hours.minutes)
    }
    @Test fun farFutureTaskIsNotNearTermWork() {
        val s = analyze(task(due = now + 365 * 24 * hour))
        assertEquals(0, s.next7Days.count); assertEquals(60L, s.pendingMinutes)
    }
    @Test fun allOverdueStillCountsInTotalWorkButNotUpcoming() {
        val s = analyze(task(1, due = now - hour), task(2, due = now - 2 * hour))
        assertEquals(2, s.overdue); assertEquals(2, s.critical)
        assertEquals(120L, s.pendingMinutes); assertEquals(0, s.next24Hours.count)
    }
    @Test fun localMidnightIsExcludedFromToday() {
        val midnight = Instant.parse("2026-09-24T00:00:00Z").toEpochMilli()
        val s = analyze(task(1, due = midnight - 1), task(2, due = midnight))
        assertEquals(1, s.remainingToday.count); assertEquals(2, s.next24Hours.count)
    }
    @Test fun localTodayRespectsDaylightSavingTransition() {
        val localZone = ZoneId.of("America/New_York")
        val start = Instant.parse("2026-03-08T05:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-03-09T04:00:00Z").toEpochMilli()
        val tasks = listOf(task(1, due = end - 1), task(2, due = end))
        val s = TaskAnalytics.calculate(tasks, start, localZone).summary
        assertEquals(1, s.remainingToday.count); assertEquals(2, s.next24Hours.count)
    }
    @Test fun nearestIncludesOldestPendingOverdueWithIdTieBreak() {
        val s = analyze(task(3, due = now - hour), task(2, due = now - hour), task(1, due = now - 2 * hour, done = true))
        assertEquals(2L, s.nearest!!.id)
    }
    @Test fun rollingWindowsAreCumulative() {
        val s = analyze(task(1), task(2, due = now + 48 * hour), task(3, due = now + 144 * hour))
        assertEquals(1, s.next24Hours.count); assertEquals(2, s.next3Days.count); assertEquals(3, s.next7Days.count)
    }
    @Test fun workloadFormattingDoesNotClampZeroToOne() {
        assertEquals("0 min", formatWorkload(0)); assertEquals("45 min", formatWorkload(45))
        assertEquals("2h 30m", formatWorkload(150)); assertEquals("8h", formatWorkload(480))
        assertEquals("14h 20m", formatWorkload(860))
    }
}
