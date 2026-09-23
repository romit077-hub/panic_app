package com.example.panic_app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.domain.risk.RiskResult
import com.example.panic_app.domain.analytics.AnalyticsSummary
import com.example.panic_app.data.repository.TaskRepository
import com.example.panic_app.ui.taskFailure
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class TasksUiState(
    val loading: Boolean = true,
    val tasks: List<TaskEntity> = emptyList(),
    val loadError: String? = null,
    val actionError: String? = null,
    val busyIds: Set<Long> = emptySet(),
    val risks: Map<Long, RiskResult> = emptyMap(),
    val rankedPendingIds: List<Long> = emptyList(),
    val calculatedAtMillis: Long = 0,
    val analytics: AnalyticsSummary = AnalyticsSummary()
) {
    val pendingCount: Int get() = analytics.pending
    val completedCount: Int get() = analytics.completed
    val highCount: Int get() = analytics.high
    val criticalCount: Int get() = analytics.critical
    val attentionCount: Int get() = risks.values.count { it.needsAttention }
    val immediateCount: Int get() = risks.values.count { it.needsImmediateAttention }
    val overdueCount: Int get() = analytics.overdue
    fun rankedPending(): List<TaskEntity> {
        val byId = tasks.associateBy { it.id }
        return rankedPendingIds.mapNotNull { byId[it] }
    }
}

class TasksViewModel(private val repository: TaskRepository,
    private val clock: () -> Long = System::currentTimeMillis) : ViewModel() {
    companion object { const val RISK_REFRESH_INTERVAL_MILLIS = 30_000L }
    private val mutableState = MutableStateFlow(TasksUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null
    init { retry() }

    fun retry() {
        observation?.cancel()
        mutableState.update { it.copy(loading = true, loadError = null) }
        observation = viewModelScope.launch {
            try {
                repository.observeAll().collect { tasks ->
                    mutableState.update { it.withRisk(tasks, clock()).copy(loading = false, loadError = null) }
                }
            } catch (error: Exception) {
                val message = taskFailure(error, "load tasks")
                mutableState.update { it.copy(loading = false, loadError = message) }
            }
        }
    }
    fun refreshTime() {
        val now = clock()
        mutableState.update { it.withRisk(it.tasks, now) }
    }
    fun clearActionError() { mutableState.update { it.copy(actionError = null) } }
    fun delete(id: Long) = perform(id, "delete task") { repository.deleteTask(id) }
    fun toggle(id: Long) = perform(id, "change completion") { repository.toggleCompletion(id) }
    private fun perform(id: Long, action: String, operation: suspend () -> Unit) {
        if (id in mutableState.value.busyIds) return
        mutableState.update { it.copy(busyIds = it.busyIds + id, actionError = null) }
        viewModelScope.launch {
            try { operation() }
            catch (error: Exception) {
                val message = taskFailure(error, action)
                mutableState.update { it.copy(actionError = message) }
            } finally { mutableState.update { it.copy(busyIds = it.busyIds - id) } }
        }
    }
}
