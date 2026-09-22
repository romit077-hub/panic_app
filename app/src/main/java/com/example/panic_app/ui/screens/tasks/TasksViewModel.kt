package com.example.panic_app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panic_app.data.local.TaskEntity
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
    val busyIds: Set<Long> = emptySet()
)

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {
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
                    mutableState.update { it.copy(loading = false, tasks = tasks, loadError = null) }
                }
            } catch (error: Exception) {
                val message = taskFailure(error, "load tasks")
                mutableState.update { it.copy(loading = false, loadError = message) }
            }
        }
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
