package com.example.panic_app.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Presentation fixtures only. Scores are supplied examples, never calculated predictions.
enum class RiskLevel { SAFE, WARNING, HIGH, CRITICAL }

data class DemoTask(
    val id: Int,
    val title: String,
    val subject: String,
    val due: LocalDateTime,
    val estimate: String,
    val priority: String,
    val risk: RiskLevel,
    val score: Int,
    val completed: Boolean = false,
    val action: String = "Break this task into a small first step."
) {
    val deadlineLabel: String get() = due.format(DateTimeFormatter.ofPattern("dd MMM • h:mm a"))
}

object DemoData {
    private val now = LocalDateTime.now()
    val tasks = listOf(
        DemoTask(1, "Computer Networks Assignment", "CN", now.plusHours(10), "3h estimate", "High", RiskLevel.CRITICAL, 88, action = "Start this task now. Aim to finish the first section in 30 minutes."),
        DemoTask(2, "DBMS Mini Project", "DBMS", now.plusDays(2), "2h estimate", "Medium", RiskLevel.HIGH, 67, action = "Allocate at least 2 hours today."),
        DemoTask(3, "CAO Revision", "CAO", now.plusDays(4), "1h estimate", "Low", RiskLevel.SAFE, 24),
        DemoTask(4, "DBMS Report", "DBMS", now.plusDays(1), "2h estimate", "Medium", RiskLevel.WARNING, 48),
        DemoTask(5, "Lab Record Submission", "CNS", now.minusHours(2), "30m estimate", "High", RiskLevel.CRITICAL, 96, action = "Finish the record and contact your instructor about submission."),
        DemoTask(6, "Python Practice", "Python", now.minusDays(1), "45m estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(7, "Java Worksheet", "Java", now.minusDays(2), "1h estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(8, "OS Notes", "OS", now.minusDays(2), "1h estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(9, "CN Quiz", "CN", now.minusDays(3), "30m estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(10, "CAO Worksheet", "CAO", now.minusDays(3), "1h estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(11, "DBMS Queries", "DBMS", now.minusDays(4), "1h estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(12, "CNS Reading", "CNS", now.minusDays(4), "45m estimate", "Low", RiskLevel.SAFE, 0, completed = true),
        DemoTask(13, "DAA Practice", "DAA", now.minusDays(5), "1h estimate", "Low", RiskLevel.SAFE, 0, completed = true)
    )
    val pending = tasks.filterNot { it.completed }
    val completed = tasks.filter { it.completed }
    val urgent = pending.filter { it.risk == RiskLevel.CRITICAL || it.risk == RiskLevel.HIGH }.sortedByDescending { it.score }
    val overdue = pending.filter { it.due.isBefore(now) }
    val nextDeadline = pending.filter { !it.due.isBefore(now) }.minBy { it.due }
}
