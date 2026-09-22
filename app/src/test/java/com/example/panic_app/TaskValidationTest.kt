package com.example.panic_app

import com.example.panic_app.model.TaskDraft
import org.junit.Assert.*
import org.junit.Test

class TaskValidationTest {
    private val valid = TaskDraft(title = "CN assignment", subject = "CN")
    @Test fun blankRequiredFieldsAreRejected() {
        val errors = valid.copy(title = "  ", subject = "\n").errors()
        assertTrue(errors.containsKey("title")); assertTrue(errors.containsKey("subject"))
    }
    @Test fun durationMustBePositiveBoundedInteger() {
        listOf("", "0", "-1", "1.5", "10081", "999999999999").forEach {
            assertTrue("Expected invalid duration: $it", valid.copy(estimatedMinutes = it).errors().containsKey("estimate"))
        }
        listOf("1", "60", "10080").forEach { assertTrue(valid.copy(estimatedMinutes = it).errors().isEmpty()) }
    }
    @Test fun pastDeadlinesAreAllowedButOutOfRangeDatesAreNot() {
        assertTrue(valid.copy(dueDateMillis = 1700000000000).errors().isEmpty())
        assertTrue(valid.copy(dueDateMillis = Long.MAX_VALUE).errors().containsKey("deadline"))
    }
    @Test fun oversizedTextIsRejected() {
        val errors = valid.copy(title = "x".repeat(161), subject = "x".repeat(81), description = "x".repeat(4001)).errors()
        assertEquals(setOf("title", "subject", "description"), errors.keys)
    }
}
