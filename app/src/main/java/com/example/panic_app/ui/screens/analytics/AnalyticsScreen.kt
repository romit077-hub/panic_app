package com.example.panic_app.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.domain.analytics.*
import com.example.panic_app.domain.risk.RiskLevel
import com.example.panic_app.model.TaskPriority
import com.example.panic_app.ui.components.*
import com.example.panic_app.ui.screens.tasks.TasksUiState
import com.example.panic_app.util.formatDeadline

@Composable
fun AnalyticsScreen(state: TasksUiState, onRetry: () -> Unit, onPanic: () -> Unit) {
    val summary = state.analytics
    ScreenList {
        item { ScreenHeading("Your productivity", "Completion, risk and workload from your saved tasks.") }
        when {
            state.loading -> item { TaskLoading() }
            state.loadError != null -> item { TaskError(state.loadError, onRetry) }
            else -> {
                item { CompletionOverview(summary) }
                item { StatsRow("Total tasks", summary.total, "Pending", summary.pending) }
                item { StatsRow("Completed", summary.completed, "Overdue", summary.overdue) }
                item { StatsRow("High risk", summary.high, "Critical", summary.critical) }
                item { SectionHeader("Workload") }
                item { Panel {
                    Text(formatWorkload(summary.pendingMinutes), style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Total estimated pending work", style = MaterialTheme.typography.titleMedium)
                    Text("Includes overdue tasks. Estimates describe remaining work, not measured study time.", style = MaterialTheme.typography.bodySmall)
                } }
                item { SectionHeader("Upcoming workload") }
                item { Panel {
                    Text("Cumulative windows from now. Longer windows include shorter ones; do not add them together. Overdue tasks are excluded.", style = MaterialTheme.typography.bodySmall)
                    WorkloadRow("Remaining today", summary.remainingToday)
                    WorkloadRow("Within 24 hours", summary.next24Hours)
                    WorkloadRow("Within 3 days", summary.next3Days)
                    WorkloadRow("Within 7 days", summary.next7Days)
                    Text("Today ends at local midnight; other windows use elapsed hours.", style = MaterialTheme.typography.bodySmall)
                } }
                item { Panel {
                    Text("Nearest pending deadline", style = MaterialTheme.typography.titleMedium)
                    val nearest = summary.nearest
                    if (nearest == null) Text("No pending deadlines") else {
                        Text(nearest.title, style = MaterialTheme.typography.titleLarge)
                        Text(formatDeadline(nearest.dueDateMillis))
                        Text(if (nearest.dueDateMillis < state.calculatedAtMillis) "Already overdue — review in PANIC Mode." else "Earliest deadline among pending tasks.")
                    }
                } }
                item { SectionHeader("Risk distribution", "Panic Mode", onPanic) }
                item { Panel {
                    Text("Pending tasks only • ${summary.pending} total", style = MaterialTheme.typography.bodySmall)
                    RiskLevel.entries.forEach { level ->
                        RiskBadge(level)
                        DistributionBar(summary.riskCounts.getValue(level), summary.pending)
                    }
                } }
                item { SectionHeader("Priority distribution") }
                item { Panel {
                    Text("Pending tasks only", style = MaterialTheme.typography.bodySmall)
                    TaskPriority.entries.forEach { priority ->
                        Text("${priority.label} priority", style = MaterialTheme.typography.titleSmall)
                        DistributionBar(summary.priorityCounts.getValue(priority), summary.pending)
                    }
                } }
                item { SectionHeader("Productivity insights") }
                item { Panel {
                    summary.insights.forEach { Text(it.message, style = MaterialTheme.typography.bodyMedium) }
                    Text("Rule-based observations from this task snapshot. No completion history or streak is inferred.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } }
            }
        }
    }
}

@Composable
private fun CompletionOverview(summary: AnalyticsSummary) {
    Panel {
        Text("Completion rate", style = MaterialTheme.typography.titleMedium)
        Text(summary.completionPercent?.let { "$it%" } ?: "—", style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary)
        if (summary.total > 0) {
            LinearProgressIndicator(progress = { summary.completionFraction }, modifier = Modifier.fillMaxWidth())
            Text("${summary.completed} of ${summary.total} recorded tasks completed")
        } else Text("No tasks yet. Add a task to begin tracking your progress.")
    }
}

@Composable
private fun StatsRow(first: String, firstValue: Int, second: String, secondValue: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(first, firstValue.toString(), Modifier.weight(1f))
        StatCard(second, secondValue.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun WorkloadRow(label: String, workload: WorkloadPeriod) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text("${workload.count} task(s) • ${formatWorkload(workload.minutes)}")
    }
}

@Composable
private fun DistributionBar(count: Int, total: Int) {
    Text("$count of $total tasks", style = MaterialTheme.typography.bodySmall)
    LinearProgressIndicator(progress = { if (total == 0) 0f else count.toFloat() / total }, modifier = Modifier.fillMaxWidth())
}
