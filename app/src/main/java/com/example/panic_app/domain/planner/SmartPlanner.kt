package com.example.panic_app.domain.planner

import com.example.panic_app.domain.risk.RiskCalculator
import com.example.panic_app.domain.risk.RiskTask
import com.example.panic_app.domain.risk.RiskResult
import com.example.panic_app.domain.risk.remainingMillis
import com.example.panic_app.model.TaskPriority
import java.time.LocalDate
import java.time.ZoneId

/** Stateless greedy scheduler. Input estimates mean remaining work, not time already spent.
 * Uses existing RiskTask instead of introducing another task entity. All timestamps are epoch ms.
 * Ranking is evaluated at generation time, not repeatedly inside future sessions. */
object SmartPlanner {
    private data class Candidate(val task: RiskTask, val risk: RiskResult)
    fun generatePlan(tasks: List<RiskTask>, config: PlannerConfig, nowMillis: Long,
        zone: ZoneId, taskTitles: Map<Long, String> = emptyMap()): StudyPlan {
        require(tasks.map { it.id }.distinct().size == tasks.size) { "Task IDs must be unique" }
        val (windows, horizonEnd) = plannerWindows(config, nowMillis, zone)
        val ranked = tasks.filterNot { it.isCompleted }.map { Candidate(it, RiskCalculator.calculate(it, nowMillis)) }
            .sortedWith(compareByDescending<Candidate> { it.risk.isOverdue }
                .thenByDescending { it.risk.score }.thenBy { it.task.dueDateMillis }
                .thenByDescending { priority(it.task.priority) }.thenBy { it.task.id })
        val remaining = ranked.associate { it.task.id to it.task.estimatedMinutes.coerceAtLeast(0).toLong() }.toMutableMap()
        val studyByDate = mutableMapOf<LocalDate, Int>()
        val dailyBlocked = mutableSetOf<Long>()
        val sessions = mutableListOf<StudySession>()
        var previousEnd: Long? = null
        fun title(id: Long) = taskTitles[id] ?: "Task $id"
        for (window in windows) {
            var cursor = window.start
            while (cursor < window.end) {
                val last = previousEnd
                if (last != null) cursor = maxOf(cursor, addClamped(last, config.breakDurationMinutes * MINUTE_MILLIS))
                if (window.end - cursor < MINUTE_MILLIS) break
                val eligible = ranked.filter { remaining.getValue(it.task.id) > 0 &&
                    it.task.dueDateMillis > nowMillis && remainingMillis(it.task.dueDateMillis, cursor) >= MINUTE_MILLIS }
                if (eligible.isEmpty()) break
                val dailyRemaining = config.maxStudyMinutesPerDay - studyByDate.getOrDefault(window.date, 0)
                if (dailyRemaining == 0) {
                    dailyBlocked += eligible.map { it.task.id }
                    break
                }
                val candidate = eligible.first()
                val task = candidate.task
                val duration = minOf(config.sessionDurationMinutes.toLong(), remaining.getValue(task.id),
                    dailyRemaining.toLong(), (minOf(window.end, task.dueDateMillis) - cursor) / MINUTE_MILLIS).toInt()
                check(duration > 0)
                val end = cursor + duration * MINUTE_MILLIS
                sessions += StudySession(task.id, title(task.id), cursor, end, duration, candidate.risk.level, candidate.risk.score)
                remaining[task.id] = remaining.getValue(task.id) - duration
                studyByDate[window.date] = studyByDate.getOrDefault(window.date, 0) + duration
                previousEnd = end
                cursor = end
            }
        }
        val unscheduled = ranked.mapNotNull { candidate ->
            val task = candidate.task
            val left = remaining.getValue(task.id)
            if (left == 0L && task.estimatedMinutes > 0) return@mapNotNull null
            val reason = when {
                task.dueDateMillis <= nowMillis -> UnscheduledReason.DEADLINE_ALREADY_PASSED
                task.estimatedMinutes <= 0 -> UnscheduledReason.INVALID_ESTIMATE
                windows.isEmpty() -> UnscheduledReason.NO_AVAILABILITY
                task.id in dailyBlocked -> UnscheduledReason.DAILY_LIMIT_REACHED
                task.dueDateMillis > horizonEnd -> UnscheduledReason.PLANNING_HORIZON_REACHED
                else -> UnscheduledReason.INSUFFICIENT_TIME_BEFORE_DEADLINE
            }
            val required = task.estimatedMinutes.coerceAtLeast(0).toLong()
            UnscheduledWork(task.id, title(task.id), required, required - left, left, reason)
        }
        return StudyPlan(sessions.toList(), unscheduled, nowMillis, horizonEnd)
    }
    private fun addClamped(time: Long, duration: Long): Long =
        if (time > Long.MAX_VALUE - duration) Long.MAX_VALUE else time + duration
    private fun priority(value: TaskPriority) = when (value) {
        TaskPriority.LOW -> 0; TaskPriority.MEDIUM -> 1; TaskPriority.HIGH -> 2
    }
}
