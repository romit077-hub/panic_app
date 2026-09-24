package com.example.panic_app.ui.screens.planner

import com.example.panic_app.data.settings.PlannerPreferences
import com.example.panic_app.domain.planner.*
import com.example.panic_app.domain.risk.RiskLevel
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class PlannerIntegrationTest {
    private val zone = ZoneId.of("UTC")
    private fun time(value: String) = Instant.parse("2026-09-21T${value}:00Z").toEpochMilli()
    private fun session(start: String, end: String) = StudySession(1, "Task", time(start), time(end), 45, RiskLevel.HIGH, 70)
    @Test fun missingSettingsUseSevenDayDefaults() {
        val config = PlannerPreferences.decode(null)
        assertEquals(7, config.availability.size)
        assertEquals(45, config.sessionDurationMinutes)
        assertEquals(600, config.availability.first { it.day == DayOfWeek.SATURDAY }.startMinute)
    }
    @Test fun multipleWindowsAndMidnightRoundTrip() {
        val config = PlannerConfig(listOf(AvailabilityWindow(DayOfWeek.MONDAY, 420, 480),
            AvailabilityWindow(DayOfWeek.MONDAY, 1080, 1440)), 30, 15, 180, 14)
        assertEquals(config, PlannerPreferences.decode(PlannerPreferences.encode(config)))
    }
    @Test fun disabledDaysStayDisabledAfterRestartMapping() {
        val config = PlannerConfig(emptyList())
        assertEquals(config, PlannerPreferences.decode(PlannerPreferences.encode(config)))
    }
    @Test fun corruptAndUnknownVersionsFallBack() {
        listOf("garbage", "2|45|10|240|7|", "1|bad|10|240|7|").forEach {
            assertEquals(PlannerPreferences.defaults, PlannerPreferences.decode(it))
        }
    }
    @Test fun invalidWindowsFallBackSafely() {
        listOf("1|45|10|240|7|1,1200,1000", "1|45|10|240|7|8,100,200", "1|0|10|240|7|").forEach {
            assertEquals(PlannerPreferences.defaults, PlannerPreferences.decode(it))
        }
    }
    @Test fun validZeroDailyBudgetIsNotReplacedByDefaults() {
        val config = PlannerConfig(maxStudyMinutesPerDay = 0)
        assertEquals(config, PlannerPreferences.decode(PlannerPreferences.encode(config)))
    }
    @Test fun allReasonsHaveReadableLabels() {
        UnscheduledReason.entries.forEach { assertFalse(it.explanation().contains('_')); assertTrue(it.explanation().isNotBlank()) }
        assertEquals("Deadline already passed", UnscheduledReason.DEADLINE_ALREADY_PASSED.explanation())
    }
    @Test fun exactRestInsideWindowIsBreak() {
        assertTrue(visibleBreak(session("18:00", "18:45"), session("18:55", "19:40"), PlannerPreferences.defaults, zone))
    }
    @Test fun separateWindowsAreNotFakeBreak() {
        val config = PlannerConfig(listOf(AvailabilityWindow(DayOfWeek.MONDAY, 1080, 1125), AvailabilityWindow(DayOfWeek.MONDAY, 1135, 1200)))
        assertFalse(visibleBreak(session("18:00", "18:45"), session("18:55", "19:40"), config, zone))
    }
    @Test fun longGapIsNotLabelledBreak() {
        assertFalse(visibleBreak(session("18:00", "18:45"), session("20:00", "20:45"), PlannerPreferences.defaults, zone))
    }
    @Test fun overlappingWindowsMergeForBreakPresentation() {
        val config = PlannerConfig(listOf(AvailabilityWindow(DayOfWeek.MONDAY, 1080, 1140), AvailabilityWindow(DayOfWeek.MONDAY, 1100, 1200)))
        assertTrue(visibleBreak(session("18:00", "18:45"), session("18:55", "19:40"), config, zone))
    }
    @Test fun localTodayDoesNotUseUtcDate() {
        assertEquals("2026-09-22", plannerDate(time("23:30"), ZoneId.of("Asia/Kolkata")).toString())
    }
    @Test fun durationsDoNotLoseRemaindersOrOverflow() {
        assertEquals("2h 15m", studyDuration(135))
        assertEquals("0 min", studyDuration(0))
        assertEquals("35791394h 7m", studyDuration(Int.MAX_VALUE.toLong()))
    }
}
