package com.example.panic_app.domain.risk

import org.junit.Assert.*
import org.junit.Test

class RiskTimeTest {
    @Test fun humanReadableMinutesHoursAndDays() {
        assertEquals("45 minutes remaining", formatRemainingTime(45 * MILLIS_PER_MINUTE))
        assertEquals("4 hours 35 minutes remaining", formatRemainingTime(275 * MILLIS_PER_MINUTE))
        assertEquals("8 days remaining", formatRemainingTime(8 * 24 * 60 * MILLIS_PER_MINUTE))
    }
    @Test fun overdueNowAndSubMinuteDurations() {
        assertEquals("Overdue by 3 hours", formatRemainingTime(-180 * MILLIS_PER_MINUTE))
        assertEquals("Due now", formatRemainingTime(0))
        assertEquals("Overdue by less than 1 minute", formatRemainingTime(-1))
    }
    @Test fun minimumLongDoesNotCreateNegativeMagnitudeText() {
        val text = formatRemainingTime(Long.MIN_VALUE)
        assertTrue(text.startsWith("Overdue by ")); assertFalse(text.contains("-"))
    }
    @Test fun multiDayWorkDoesNotDiscardRemainingMinutes() {
        assertEquals("1 day 30 minutes", formatWorkMinutes(1470))
        assertEquals("2 days 1 hour 5 minutes", formatWorkMinutes(2945))
        assertEquals("Overdue by 1 day 30 minutes", formatRemainingTime(-1470 * MILLIS_PER_MINUTE))
    }
}
