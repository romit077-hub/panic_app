package com.example.panic_app.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// A form draft is not a second task store. Only Room owns saved tasks.
data class TaskDraft(
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val dueDateMillis: Long = LocalDate.now().plusDays(1).atTime(23, 59)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    val estimatedMinutes: String = "60",
    val priority: TaskPriority = TaskPriority.MEDIUM
) {
    fun errors(): Map<String, String> = buildMap {
        if (title.isBlank()) put("title", "Enter a task title.")
        else if (title.trim().length > 160) put("title", "Use 160 characters or fewer.")
        if (subject.isBlank()) put("subject", "Enter a subject.")
        else if (subject.trim().length > 80) put("subject", "Use 80 characters or fewer.")
        if (description.length > 4000) put("description", "Use 4,000 characters or fewer.")
        val minutes = estimatedMinutes.toIntOrNull()
        if (minutes == null || minutes !in 1..10080) put("estimate", "Enter 1–10,080 minutes (up to one week).")
        val year = Instant.ofEpochMilli(dueDateMillis).atZone(ZoneId.systemDefault()).year
        if (year !in 1970..2100) put("deadline", "Choose a deadline between 1970 and 2100.")
    }
}
