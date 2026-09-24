package com.example.panic_app.domain.planner

import com.example.panic_app.domain.risk.RiskTask
import com.example.panic_app.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class SmartPlannerTest {
    private val now = Instant.parse("2026-09-21T18:00:00Z").toEpochMilli() // Monday
    private val minute = 60_000L
    private val zone = ZoneId.of("UTC")
    private fun task(id: Long = 1, minutes: Int = 120, due: Long = now + 24 * 60 * minute,
        priority: TaskPriority = TaskPriority.LOW, done: Boolean = false) = RiskTask(id, due, minutes, priority, done)
    private fun window(start: Int = 1080, end: Int = 1320, day: DayOfWeek = DayOfWeek.MONDAY) = AvailabilityWindow(day, start, end)
    private fun config(vararg windows: AvailabilityWindow) = PlannerConfig(availability = windows.toList())
    private val normal get() = config(window())
    private fun plan(tasks: List<RiskTask>, config: PlannerConfig = normal, time: Long = now) =
        SmartPlanner.generatePlan(tasks, config, time, zone)

    @Test fun completedTasksExcluded() {
        val p = plan(listOf(task(done = true)))
        assertTrue(p.sessions.isEmpty()); assertTrue(p.unscheduledWork.isEmpty())
    }
    @Test fun criticalTaskScheduledBeforeSafeTask() {
        val p = plan(listOf(task(1, 20, now + 20 * 24 * 60 * minute), task(2, 60, now + 90 * minute)))
        assertEquals(2L, p.sessions.first().taskId)
    }
    @Test fun earlierDeadlineWinsEqualRiskTie() {
        val p = plan(listOf(task(1, 10, now + 5 * 24 * 60 * minute), task(2, 10, now + 4 * 24 * 60 * minute)))
        assertEquals(2L, p.sessions.first().taskId)
    }
    @Test fun splits120Into45Plus45Plus30() {
        assertEquals(listOf(45, 45, 30), plan(listOf(task())).sessions.map { it.durationMinutes })
    }
    @Test fun twentyMinuteTaskStaysTwenty() {
        assertEquals(listOf(20), plan(listOf(task(minutes = 20))).sessions.map { it.durationMinutes })
    }
    @Test fun sessionsNeverOverlap() {
        val p = plan(listOf(task(1), task(2), task(3)))
        p.sessions.zipWithNext().forEach { (a, b) -> assertTrue(a.endTime <= b.startTime) }
    }
    @Test fun sessionsStayInsideAvailabilityAndLastSessionIsShortened() {
        val p = plan(listOf(task()), config(window(end = 1140)))
        assertEquals(listOf(45, 5), p.sessions.map { it.durationMinutes })
        p.sessions.forEach { assertTrue(it.startTime >= now && it.endTime <= now + 60 * minute) }
    }
    @Test fun breakConsumesClockTimeButNotWork() {
        val p = plan(listOf(task()))
        assertEquals(now + 55 * minute, p.sessions[1].startTime)
        assertEquals(now + 140 * minute, p.sessions.last().endTime)
        assertEquals(120L, p.totalScheduledMinutes)
    }
    @Test fun dailyLimitAppliesAcrossWindows() {
        val p = plan(listOf(task(minutes = 200)), config(window(end = 1140), window(1200, 1320)).copy(maxStudyMinutesPerDay = 60))
        assertEquals(60L, p.totalScheduledMinutes)
        assertEquals(UnscheduledReason.DAILY_LIMIT_REACHED, p.unscheduledWork.single().reason)
    }
    @Test fun currentTimeClipsAvailability() {
        val time = now + 23 * minute + 17_000L
        val p = plan(listOf(task()), time = time)
        assertEquals(time, p.sessions.first().startTime)
        assertTrue(p.sessions.all { it.startTime >= time })
    }
    @Test fun insufficientWindowReportsRemainder() {
        val p = plan(listOf(task()), config(window(end = 1140)))
        assertEquals(70L, p.totalUnscheduledMinutes)
        assertEquals(UnscheduledReason.INSUFFICIENT_TIME_BEFORE_DEADLINE, p.unscheduledWork.single().reason)
    }
    @Test fun overdueWorkIsExplicitAndNeverScheduled() {
        val p = plan(listOf(task(due = now - 1)))
        assertTrue(p.sessions.isEmpty()); assertEquals(120L, p.totalUnscheduledMinutes)
        assertEquals(UnscheduledReason.DEADLINE_ALREADY_PASSED, p.unscheduledWork.single().reason)
    }
    @Test fun nonpositiveEstimateIsAttentionWithNoInventedMinutes() {
        val p = plan(listOf(task(1, 0), task(2, -10)))
        assertTrue(p.sessions.isEmpty()); assertEquals(0L, p.totalUnscheduledMinutes)
        assertTrue(p.unscheduledWork.all { it.reason == UnscheduledReason.INVALID_ESTIMATE })
    }
    @Test fun noAvailabilityReturnsAllWork() {
        val p = plan(listOf(task()), PlannerConfig())
        assertEquals(120L, p.totalUnscheduledMinutes)
        assertEquals(UnscheduledReason.NO_AVAILABILITY, p.unscheduledWork.single().reason)
    }
    @Test fun multipleWindowsCanCoverTask() {
        val p = plan(listOf(task(minutes = 80)), config(window(end = 1120), window(1200, 1240)))
        assertEquals(listOf(40, 40), p.sessions.map { it.durationMinutes })
        assertEquals(now + 120 * minute, p.sessions.last().startTime)
    }
    @Test fun overlappingAndAdjacentWindowsAreMerged() {
        val p = plan(listOf(task()), config(window(1080, 1140), window(1110, 1200), window(1200, 1260)))
        assertEquals(listOf(45, 45, 30), p.sessions.map { it.durationMinutes })
        assertEquals(120L, p.totalScheduledMinutes)
    }
    @Test fun neverSchedulesAfterDeadline() {
        val deadline = now + 65 * minute
        val p = plan(listOf(task(due = deadline)))
        assertEquals(listOf(45, 10), p.sessions.map { it.durationMinutes })
        assertEquals(65L, p.totalUnscheduledMinutes)
        assertTrue(p.sessions.all { it.endTime <= deadline })
    }
    @Test fun deterministicEvenIfInputOrderReversed() {
        val tasks = listOf(task(2, 30), task(1, 30))
        assertEquals(plan(tasks), plan(tasks.reversed()))
        assertEquals(1L, plan(tasks).sessions.first().taskId)
    }
    @Test fun hugeWorkloadUsesLongTotals() {
        val p = plan(listOf(task(1, Int.MAX_VALUE), task(2, Int.MAX_VALUE)))
        assertEquals(4_294_967_294L, p.totalScheduledMinutes + p.totalUnscheduledMinutes)
        assertTrue(p.sessions.size < 10)
    }
    @Test fun workIsConservedAcrossTasksAndPartialSessions() {
        val tasks = listOf(task(1, 120), task(2, 20), task(3, 90, now - 1), task(4, 15, done = true))
        val p = plan(tasks)
        assertEquals(230L, p.totalScheduledMinutes + p.totalUnscheduledMinutes)
        tasks.filterNot { it.isCompleted }.forEach { t ->
            val scheduled = p.sessions.filter { it.taskId == t.id }.sumOf { it.durationMinutes.toLong() }
            val remaining = p.unscheduledWork.firstOrNull { it.taskId == t.id }?.unscheduledMinutes ?: 0L
            assertEquals(t.estimatedMinutes.toLong(), scheduled + remaining)
        }
    }
    @Test fun dailyBudgetResetsNextDay() {
        val p = plan(listOf(task(minutes = 120, due = now + 3 * 24 * 60 * minute)),
            config(window(), window(day = DayOfWeek.TUESDAY)).copy(maxStudyMinutesPerDay = 60))
        assertEquals(120L, p.totalScheduledMinutes)
        assertEquals(2, p.sessions.map { Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate() }.distinct().size)
    }
    @Test fun horizonLimitsFutureWork() {
        val p = plan(listOf(task(minutes = 600, due = now + 30 * 24 * 60 * minute)), normal.copy(horizonDays = 1, maxStudyMinutesPerDay = 1440))
        assertEquals(UnscheduledReason.PLANNING_HORIZON_REACHED, p.unscheduledWork.single().reason)
        assertTrue(p.sessions.all { it.endTime <= p.horizonEnd })
    }
    @Test fun breakIsRespectedAcrossCloseWindows() {
        val p = plan(listOf(task(minutes = 80)), config(window(1080, 1120), window(1125, 1200)))
        assertEquals(now + 50 * minute, p.sessions[1].startTime)
    }
    @Test fun taskAtDeadlineCannotReceiveFutureWork() {
        assertEquals(UnscheduledReason.DEADLINE_ALREADY_PASSED, plan(listOf(task(due = now))).unscheduledWork.single().reason)
    }
    @Test fun zeroDailyLimitReturnsWorkWithoutLooping() {
        val p = plan(listOf(task()), normal.copy(maxStudyMinutesPerDay = 0))
        assertTrue(p.sessions.isEmpty()); assertEquals(UnscheduledReason.DAILY_LIMIT_REACHED, p.unscheduledWork.single().reason)
    }
    @Test fun regenerationReflectsCompletionWithoutMutatingInput() {
        val tasks = listOf(task())
        assertTrue(plan(tasks).sessions.isNotEmpty())
        assertTrue(plan(tasks.map { it.copy(isCompleted = true) }).sessions.isEmpty())
        assertFalse(tasks.single().isCompleted)
    }
    @Test fun higherPriorityBreaksEqualCriticalScoreAndDeadlineTie() {
        val p = plan(listOf(task(1, 120, now + 30 * minute, TaskPriority.LOW), task(2, 120, now + 30 * minute, TaskPriority.HIGH)))
        assertEquals(2L, p.sessions.first().taskId)
    }
    @Test fun suppliedTitlesAndSummaryAreDerivedFromSessions() {
        val p = SmartPlanner.generatePlan(listOf(task(due = now + 60 * minute)), normal, now, zone, mapOf(1L to "CN Assignment"))
        assertEquals("CN Assignment", p.sessions.first().taskTitle)
        assertEquals(1, p.tasksCovered); assertEquals(1, p.criticalTasksIncluded)
    }
    @Test(expected = IllegalArgumentException::class) fun invalidConfigRejected() { normal.copy(sessionDurationMinutes = 0) }
    @Test(expected = IllegalArgumentException::class) fun duplicateIdsRejected() { plan(listOf(task(), task())) }
    @Test fun dstGapEndpointIsNotSilentlyShiftedIntoUnavailableTime() {
        val time = Instant.parse("2026-03-08T05:00:00Z").toEpochMilli()
        val cfg = config(window(150, 240, DayOfWeek.SUNDAY)).copy(horizonDays = 1)
        val p = SmartPlanner.generatePlan(listOf(task(due = time + 24 * 60 * minute)), cfg, time, ZoneId.of("America/New_York"))
        assertTrue(p.sessions.isEmpty()); assertEquals(UnscheduledReason.NO_AVAILABILITY, p.unscheduledWork.single().reason)
    }
}
