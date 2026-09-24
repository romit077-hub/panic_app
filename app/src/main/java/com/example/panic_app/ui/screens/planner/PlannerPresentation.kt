package com.example.panic_app.ui.screens.planner

import com.example.panic_app.domain.planner.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun UnscheduledReason.explanation(): String = when (this) {
    UnscheduledReason.NO_AVAILABILITY -> "No available study time"
    UnscheduledReason.INSUFFICIENT_TIME_BEFORE_DEADLINE -> "Not enough time before deadline"
    UnscheduledReason.DAILY_LIMIT_REACHED -> "Daily study limit reached"
    UnscheduledReason.DEADLINE_ALREADY_PASSED -> "Deadline already passed"
    UnscheduledReason.PLANNING_HORIZON_REACHED -> "Work extends beyond the planning horizon"
    UnscheduledReason.INVALID_ESTIMATE -> "Update this task's work estimate"
}
fun plannerDate(time: Long, zone: ZoneId) = Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
fun plannerTime(time: Long, zone: ZoneId): String = Instant.ofEpochMilli(time).atZone(zone)
    .format(DateTimeFormatter.ofPattern("h:mm a"))
fun windowTime(minute: Int): String = if (minute == 1440) "Midnight (end of day)"
    else java.time.LocalTime.of(minute / 60, minute % 60).format(DateTimeFormatter.ofPattern("h:mm a"))
fun studyDuration(minutes: Long): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0L -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
/** Only an exact configured gap inside one merged availability interval is labelled a break. */
fun visibleBreak(first: StudySession, next: StudySession, config: PlannerConfig, zone: ZoneId): Boolean {
    if (config.breakDurationMinutes == 0 || next.startTime - first.endTime != config.breakDurationMinutes * 60_000L) return false
    if (plannerDate(first.startTime, zone) != plannerDate(next.startTime, zone)) return false
    val (windows, _) = plannerWindows(config.copy(horizonDays = 1), first.startTime, zone)
    return windows.any { first.startTime >= it.start && next.endTime <= it.end }
}
