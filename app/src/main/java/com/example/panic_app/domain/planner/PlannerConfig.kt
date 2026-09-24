package com.example.panic_app.domain.planner

import java.time.DayOfWeek

/** Weekly local-time window. Minutes after midnight; end=1440 means next midnight.
 * Overnight periods must be split at midnight into two day-specific windows. */
data class AvailabilityWindow(val day: DayOfWeek, val startMinute: Int, val endMinute: Int) {
    init { require(startMinute in 0..1439 && endMinute in 1..1440 && startMinute < endMinute) }
}

data class PlannerConfig(
    val availability: List<AvailabilityWindow> = emptyList(),
    val sessionDurationMinutes: Int = 45,
    val breakDurationMinutes: Int = 10,
    val maxStudyMinutesPerDay: Int = 240,
    val horizonDays: Int = 7
) {
    init {
        require(sessionDurationMinutes in 1..1440)
        require(breakDurationMinutes in 0..1440)
        require(maxStudyMinutesPerDay in 0..1440)
        require(horizonDays in 1..366)
    }
}
