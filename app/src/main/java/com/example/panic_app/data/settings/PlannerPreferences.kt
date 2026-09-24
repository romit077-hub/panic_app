package com.example.panic_app.data.settings

import com.example.panic_app.domain.planner.*
import java.time.DayOfWeek

/** Versioned, pure preference codec. Missing values use defaults; an empty window string stays empty. */
object PlannerPreferences {
    val defaults = PlannerConfig(availability = DayOfWeek.entries.map {
        AvailabilityWindow(it, if (it.value <= 5) 1080 else 600, if (it.value <= 5) 1320 else 1080)
    })
    fun encode(config: PlannerConfig): String = listOf("1", config.sessionDurationMinutes,
        config.breakDurationMinutes, config.maxStudyMinutesPerDay, config.horizonDays,
        config.availability.joinToString(";") { "${it.day.value},${it.startMinute},${it.endMinute}" }).joinToString("|")
    fun decode(raw: String?): PlannerConfig {
        if (raw == null) return defaults
        return try {
            val parts = raw.split('|')
            require(parts.size == 6 && parts[0] == "1")
            val windows = if (parts[5].isEmpty()) emptyList() else parts[5].split(';').map {
                val fields = it.split(','); require(fields.size == 3)
                AvailabilityWindow(DayOfWeek.of(fields[0].toInt()), fields[1].toInt(), fields[2].toInt())
            }
            PlannerConfig(windows, parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4].toInt())
        } catch (_: RuntimeException) { defaults }
    }
}
