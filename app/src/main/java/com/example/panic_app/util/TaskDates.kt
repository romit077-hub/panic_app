package com.example.panic_app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDeadline(timestamp: Long, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String {
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(zone)
    val dateLabel = when (dateTime.toLocalDate()) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
    }
    return "$dateLabel, ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))}"
}

// Reject nonexistent local times at a DST spring-forward transition.
// For an overlap, choose the earlier offset consistently.
fun deadlineTimestamp(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long? {
    val local = date.atTime(time)
    val offsets = zone.rules.getValidOffsets(local)
    return offsets.firstOrNull()?.let { local.toInstant(it).toEpochMilli() }
}
