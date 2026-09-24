package com.example.panic_app.domain.planner

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal const val MINUTE_MILLIS = 60_000L
internal data class PlanWindow(val date: LocalDate, val start: Long, val end: Long)

/** Merge local overlaps first. DST gap endpoints are rejected (same convention as task input).
 * Ambiguous endpoints use the earlier offset; duration is real elapsed time. */
internal fun plannerWindows(config: PlannerConfig, now: Long, zone: ZoneId): Pair<List<PlanWindow>, Long> {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    fun epoch(instant: Instant): Long = when {
        instant > Instant.ofEpochMilli(Long.MAX_VALUE) -> Long.MAX_VALUE
        instant < Instant.ofEpochMilli(Long.MIN_VALUE) -> Long.MIN_VALUE
        else -> instant.toEpochMilli()
    }
    val horizonEnd = epoch(today.plusDays(config.horizonDays.toLong()).atStartOfDay(zone).toInstant())
    val result = mutableListOf<PlanWindow>()
    repeat(config.horizonDays) { offset ->
        val date = today.plusDays(offset.toLong())
        val ranges = config.availability.filter { it.day == date.dayOfWeek }.sortedBy { it.startMinute }
        val merged = mutableListOf<Pair<Int, Int>>()
        for (range in ranges) {
            val last = merged.lastOrNull()
            if (last != null && range.startMinute <= last.second) merged[merged.lastIndex] = last.first to maxOf(last.second, range.endMinute)
            else merged += range.startMinute to range.endMinute
        }
        fun timestamp(minute: Int): Long? {
            val local = date.atStartOfDay().plusMinutes(minute.toLong())
            return zone.rules.getValidOffsets(local).firstOrNull()?.let { epoch(local.toInstant(it)) }
        }
        for ((from, to) in merged) {
            val start = timestamp(from) ?: continue
            val end = timestamp(to) ?: continue
            val clippedStart = maxOf(start, now)
            if (end - clippedStart >= MINUTE_MILLIS) result += PlanWindow(date, clippedStart, end)
        }
    }
    return result.sortedBy { it.start } to horizonEnd
}
