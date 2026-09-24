package com.example.panic_app.ui.screens.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.panic_app.domain.planner.*
import com.example.panic_app.ui.components.*
import com.example.panic_app.util.formatDeadline
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PlannerRoute(viewModel: PlannerViewModel, onEdit: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var settings by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) { now = System.currentTimeMillis(); delay(30_000) }
        }
    }
    if (settings) {
        androidx.activity.compose.BackHandler { settings = false }
        PlannerSettingsScreen(state, viewModel::save) { settings = false }
    } else PlannerScreen(state, now, viewModel::generate, viewModel::retry, { settings = true }, onEdit)
}

@Composable
private fun PlannerScreen(state: PlannerUiState, now: Long, onGenerate: () -> Unit,
    onRetry: () -> Unit, onSettings: () -> Unit, onEdit: (Long) -> Unit) {
    var upcoming by rememberSaveable { mutableStateOf(false) }
    var explanation by rememberSaveable { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val today = plannerDate(now, zone)
    val plan = state.plan
    val pending = state.tasks.count { !it.isCompleted }
    val grouped = remember(plan, zone) { plan?.sessions.orEmpty().groupBy { plannerDate(it.startTime, zone) } }
    ScreenList {
        item { ScreenHeading("Smart Planner", "Make time for the deadlines that matter.") }
        item { OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Study availability & settings") } }
        when {
            state.loading -> item { TaskLoading() }
            state.error != null -> item { TaskError(state.error, onRetry) }
            pending == 0 -> item { Panel { Text("You're clear", style = MaterialTheme.typography.titleLarge); Text("No unfinished tasks need scheduling.") } }
            else -> {
                item { Button(onClick = onGenerate, enabled = !state.generating && !state.saving, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.generating) "Building plan…" else if (plan == null) "Generate Plan" else "Regenerate Plan")
                } }
                if (state.generating) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (state.config.availability.isEmpty()) item { Panel {
                    Text("PANIC couldn't find any study time.", style = MaterialTheme.typography.titleMedium)
                    Text("Add availability in planner settings to generate a plan.")
                } }
                if (plan == null && !state.generating && state.config.availability.isNotEmpty()) item { Panel {
                    Text("Ready when you are", style = MaterialTheme.typography.titleMedium)
                    Text("Review your study availability, then generate a plan from your saved tasks.")
                } }
            }
        }
        if (plan != null && pending > 0) {
            item { Panel {
                Text("Plan summary", style = MaterialTheme.typography.titleMedium)
                Text(studyDuration(plan.totalScheduledMinutes) + " scheduled", style = MaterialTheme.typography.headlineSmall)
                Text("${plan.tasksCovered} tasks covered • ${studyDuration(plan.totalUnscheduledMinutes)} unscheduled")
                Text("Generated ${formatDeadline(plan.generatedAt, zone, today)}", style = MaterialTheme.typography.bodySmall)
                Text("Sessions are suggestions, not recorded progress. Update task estimates as you work.", style = MaterialTheme.typography.bodySmall)
                if (state.zone != zone || plannerDate(plan.generatedAt, zone) != today)
                    Text("Date or time zone changed. Regenerate for your current availability.", color = MaterialTheme.colorScheme.error)
            } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(!upcoming, { upcoming = false }, label = { Text("Today") })
                FilterChip(upcoming, { upcoming = true }, label = { Text("Upcoming") })
            } }
            val visible = grouped.filterKeys { if (upcoming) it > today else it == today }.toSortedMap()
            if (visible.isEmpty()) item { Panel {
                Text(if (upcoming) "No upcoming sessions." else "Nothing scheduled for today.")
                if (!upcoming) plan.sessions.firstOrNull { plannerDate(it.startTime, zone) > today }?.let {
                    Text("Next study session: ${formatDeadline(it.startTime, zone, today)}")
                }
            } }
            visible.forEach { (date, sessions) ->
                item(key = "date-$date") { SectionHeader(date.format(DateTimeFormatter.ofPattern("EEEE, d MMM")))
                    Text("${sessions.size} sessions • ${studyDuration(sessions.sumOf { it.durationMinutes.toLong() })}") }
                sessionTimeline(sessions, state.config, zone, onEdit)
            }
            if (plan.unscheduledWork.isEmpty()) item { Panel { Text("All current workload fits before its deadlines.") } }
            else {
                item { SectionHeader("Workload at risk") }
                items(plan.unscheduledWork, key = { "unscheduled-${it.taskId}" }) { work ->
                    Panel {
                        Text(work.taskTitle, style = MaterialTheme.typography.titleMedium)
                        Text("${studyDuration(work.unscheduledMinutes)} could not be scheduled", color = MaterialTheme.colorScheme.error)
                        state.tasks.firstOrNull { it.id == work.taskId }?.let { Text("Deadline: ${formatDeadline(it.dueDateMillis, zone, today)}") }
                        Text(work.reason.explanation())
                        TextButton(onClick = { onEdit(work.taskId) }) { Text("Review task") }
                    }
                }
            }
        }
        item { TextButton(onClick = { explanation = true }) { Text("How PANIC plans my time") } }
    }
    if (explanation) AlertDialog(onDismissRequest = { explanation = false }, title = { Text("How PANIC plans") },
        text = { Text("PANIC prioritizes unfinished work using deadline risk, remaining workload and priority. It fits study sessions into your available time before each deadline, respecting breaks and your daily study limit. Work that cannot fit is shown for review. Regenerate to plan again from the current time.") },
        confirmButton = { TextButton(onClick = { explanation = false }) { Text("Got it") } })
}

private fun LazyListScope.sessionTimeline(sessions: List<StudySession>, config: PlannerConfig, zone: ZoneId, onEdit: (Long) -> Unit) {
    sessions.forEachIndexed { index, session ->
        item(key = "session-${session.taskId}-${session.startTime}") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.width(76.dp)) {
                    Text(plannerTime(session.startTime, zone), style = MaterialTheme.typography.labelLarge)
                    Text(plannerTime(session.endTime, zone), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    VerticalDivider(Modifier.padding(start = 8.dp, top = 8.dp).height(44.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(session.taskTitle, style = MaterialTheme.typography.titleMedium)
                    RiskBadge(session.riskLevel)
                    Text("${session.durationMinutes} min • Risk ${session.riskScore}/100", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { onEdit(session.taskId) }) { Text("Open task") }
                }
            }
            val next = sessions.getOrNull(index + 1)
            if (next != null && visibleBreak(session, next, config, zone)) {
                Text("${plannerTime(session.endTime, zone)} – ${plannerTime(next.startTime, zone)} · Break • ${config.breakDurationMinutes} min",
                    Modifier.padding(start = 88.dp, top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
