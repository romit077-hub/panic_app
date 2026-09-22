package com.example.panic_app

import com.example.panic_app.util.deadlineTimestamp
import com.example.panic_app.util.formatDeadline
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class TaskDatesTest {
    @Test fun indianLocalTimeRoundTripsWithoutUtcDateShift() {
        val zone = ZoneId.of("Asia/Kolkata")
        val day = LocalDate.of(2026, 9, 24)
        val timestamp = requireNotNull(deadlineTimestamp(day, LocalTime.of(0, 15), zone))
        assertEquals(Instant.parse("2026-09-23T18:45:00Z").toEpochMilli(), timestamp)
        assertTrue(formatDeadline(timestamp, zone, day).startsWith("Today,"))
        assertTrue(formatDeadline(timestamp, zone, day.minusDays(1)).startsWith("Tomorrow,"))
    }
    @Test fun nonexistentDstTimeIsRejected() {
        assertNull(deadlineTimestamp(LocalDate.of(2026, 3, 8), LocalTime.of(2, 30), ZoneId.of("America/New_York")))
    }
    @Test fun dstOverlapChoosesEarlierInstant() {
        val timestamp = deadlineTimestamp(LocalDate.of(2026, 11, 1), LocalTime.of(1, 30), ZoneId.of("America/New_York"))
        assertEquals(Instant.parse("2026-11-01T05:30:00Z").toEpochMilli(), timestamp)
    }
}
