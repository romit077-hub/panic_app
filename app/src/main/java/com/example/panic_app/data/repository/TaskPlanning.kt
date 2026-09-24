package com.example.panic_app.data.repository

import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.planner.PlannerConfig
import com.example.panic_app.domain.planner.SmartPlanner
import com.example.panic_app.domain.planner.StudyPlan
import com.example.panic_app.domain.risk.RiskTask
import java.time.ZoneId

/** Read-only adapter for an existing repository Flow snapshot. No persistence or UI side effects. */
fun List<TaskEntity>.generateStudyPlan(config: PlannerConfig, nowMillis: Long, zone: ZoneId): StudyPlan =
    SmartPlanner.generatePlan(map { RiskTask(it.id, it.dueDateMillis, it.estimatedMinutes, it.priority, it.isCompleted) },
        config, nowMillis, zone, associate { it.id to it.title })
