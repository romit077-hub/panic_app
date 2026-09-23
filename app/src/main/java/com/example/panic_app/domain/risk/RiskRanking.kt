package com.example.panic_app.domain.risk

object RiskRanking {
    // Stable final ID tie-break prevents cards jumping when deadlines and scores are equal.
    val comparator: Comparator<AssessedTask> = compareByDescending<AssessedTask> { it.risk.isOverdue }
        .thenByDescending { it.risk.score }
        .thenBy { it.task.dueDateMillis }
        .thenBy { it.task.id }

    fun pending(assessments: List<AssessedTask>): List<AssessedTask> =
        assessments.filter { !it.task.isCompleted && it.risk.isActive }.sortedWith(comparator)
}
