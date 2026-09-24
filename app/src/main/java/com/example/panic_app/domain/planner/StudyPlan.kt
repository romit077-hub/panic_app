package com.example.panic_app.domain.planner

import com.example.panic_app.domain.risk.RiskLevel

data class StudySession(val taskId: Long, val taskTitle: String, val startTime: Long,
    val endTime: Long, val durationMinutes: Int, val riskLevel: RiskLevel, val riskScore: Int)

enum class UnscheduledReason {
    NO_AVAILABILITY, INSUFFICIENT_TIME_BEFORE_DEADLINE, DAILY_LIMIT_REACHED,
    DEADLINE_ALREADY_PASSED, PLANNING_HORIZON_REACHED, INVALID_ESTIMATE
}
data class UnscheduledWork(val taskId: Long, val taskTitle: String, val requiredMinutes: Long,
    val scheduledMinutes: Long, val unscheduledMinutes: Long, val reason: UnscheduledReason)

data class StudyPlan(val sessions: List<StudySession>, val unscheduledWork: List<UnscheduledWork>,
    val generatedAt: Long, val horizonEnd: Long) {
    val totalScheduledMinutes: Long get() = sessions.sumOf { it.durationMinutes.toLong() }
    val totalUnscheduledMinutes: Long get() = unscheduledWork.sumOf { it.unscheduledMinutes }
    val tasksCovered: Int get() = sessions.map { it.taskId }.distinct().size
    val criticalTasksIncluded: Int get() = sessions.filter { it.riskLevel == RiskLevel.CRITICAL }.map { it.taskId }.distinct().size
}
