package com.example.panic_app.domain.risk

const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR

// Saturation preserves ordering/sign even for pathological Long timestamp inputs.
fun remainingMillis(deadline: Long, now: Long): Long = try {
    Math.subtractExact(deadline, now)
} catch (_: ArithmeticException) {
    if (deadline > now) Long.MAX_VALUE else Long.MIN_VALUE
}

private fun units(value: Long, singular: String): String = "$value $singular${if (value == 1L) "" else "s"}"

fun formatWorkMinutes(minutes: Int): String = formatWholeMinutes(minutes.coerceAtLeast(1).toLong())

private fun formatWholeMinutes(minutes: Long): String {
    val days = minutes / MINUTES_PER_DAY
    val hours = (minutes % MINUTES_PER_DAY) / MINUTES_PER_HOUR
    val remainder = minutes % MINUTES_PER_HOUR
    return when {
        days > 0 -> listOfNotNull(units(days, "day"),
            if (hours > 0) units(hours, "hour") else null,
            if (remainder > 0) units(remainder, "minute") else null).joinToString(" ")
        hours > 0 -> units(hours, "hour") + if (remainder > 0) " ${units(remainder, "minute")}" else ""
        else -> units(remainder, "minute")
    }
}

fun formatRemainingTime(millis: Long): String {
    if (millis == 0L) return "Due now"
    val magnitude = if (millis == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(millis)
    val duration = if (magnitude < MILLIS_PER_MINUTE) "less than 1 minute" else formatWholeMinutes(magnitude / MILLIS_PER_MINUTE)
    return if (millis < 0) "Overdue by $duration" else "$duration remaining"
}
