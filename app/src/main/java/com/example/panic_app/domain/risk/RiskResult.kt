package com.example.panic_app.domain.risk

import com.example.panic_app.model.TaskPriority

// Pure input DTO. Room entities are mapped to this at the presentation boundary.
data class RiskTask(
    val id: Long,
    val dueDateMillis: Long,
    val estimatedMinutes: Int,
    val priority: TaskPriority,
    val isCompleted: Boolean = false
)

data class RiskResult(
    val score: Int,
    val level: RiskLevel,
    val isOverdue: Boolean,
    val isActive: Boolean,
    val remainingMillis: Long,
    val reason: String,
    val recommendedAction: String,
    val timePoints: Int = 0,
    val workloadPoints: Int = 0,
    val priorityPoints: Int = 0,
    val capacityFloorApplied: Boolean = false
) {
    val needsAttention: Boolean get() = isActive && level != RiskLevel.SAFE
    val needsImmediateAttention: Boolean get() = isActive && (level == RiskLevel.HIGH || level == RiskLevel.CRITICAL)
}

data class AssessedTask(val task: RiskTask, val risk: RiskResult)
