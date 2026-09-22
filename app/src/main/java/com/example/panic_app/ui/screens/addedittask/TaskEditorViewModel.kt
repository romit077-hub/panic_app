package com.example.panic_app.ui.screens.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panic_app.data.repository.TaskMissingException
import com.example.panic_app.data.repository.TaskRepository
import com.example.panic_app.model.TaskDraft
import com.example.panic_app.model.TaskPriority
import com.example.panic_app.ui.taskFailure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditorState(
    val draft: TaskDraft = TaskDraft(), val loading: Boolean = false,
    val saving: Boolean = false, val saved: Boolean = false,
    val loadError: String? = null, val saveError: String? = null,
    val errors: Map<String, String> = emptyMap()
)

class TaskEditorViewModel(private val repository: TaskRepository, private val handle: SavedStateHandle,
    private val taskId: Long?) : ViewModel() {
    private val mutableState = MutableStateFlow(TaskEditorState(loading = taskId != null))
    val state = mutableState.asStateFlow()
    init { load() }

    fun load() {
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, loadError = null) }
            try {
                val existing = taskId?.let { repository.getTask(it) ?: throw TaskMissingException() }
                val restored = handle.get<Boolean>("draftReady") == true
                val draft = if (restored) TaskDraft(
                    title = handle["title"] ?: "", description = handle["description"] ?: "",
                    subject = handle["subject"] ?: "", dueDateMillis = checkNotNull(handle.get<Long>("due")),
                    estimatedMinutes = handle["estimate"] ?: "60",
                    priority = TaskPriority.valueOf(handle["priority"] ?: "MEDIUM")
                ) else if (existing != null) TaskDraft(existing.title, existing.description, existing.subject,
                    existing.dueDateMillis, existing.estimatedMinutes.toString(), existing.priority) else TaskDraft()
                mutableState.update { it.copy(draft = draft, loading = false) }
                persistDraft(draft)
            } catch (error: Exception) {
                val message = taskFailure(error, "open task")
                mutableState.update { it.copy(loading = false, loadError = message) }
            }
        }
    }
    fun change(draft: TaskDraft) {
        if (state.value.saving || state.value.loading || state.value.saved) return
        mutableState.update { it.copy(draft = draft, errors = it.errors.filterKeys { key -> key == "deadline" }, saveError = null) }
        persistDraft(draft)
    }
    fun changeDeadline(timestamp: Long) {
        change(state.value.draft.copy(dueDateMillis = timestamp))
        mutableState.update { it.copy(errors = it.errors - "deadline") }
    }
    fun dateError() { mutableState.update { it.copy(errors = it.errors + ("deadline" to "This local time does not exist because clocks move forward. Choose another time.")) } }
    private fun persistDraft(draft: TaskDraft) {
        handle["title"] = draft.title; handle["description"] = draft.description
        handle["subject"] = draft.subject; handle["due"] = draft.dueDateMillis
        handle["estimate"] = draft.estimatedMinutes; handle["priority"] = draft.priority.name
        handle["draftReady"] = true
    }
    fun save() {
        val current = state.value
        if (current.loading || current.saving || current.saved || current.loadError != null) return
        val errors = current.draft.errors() + current.errors.filterKeys { it == "deadline" }
        if (errors.isNotEmpty()) { mutableState.update { it.copy(errors = errors) }; return }
        mutableState.update { it.copy(saving = true, saveError = null, errors = emptyMap()) }
        viewModelScope.launch {
            try {
                if (taskId == null) repository.addTask(current.draft) else repository.updateTask(taskId, current.draft)
                mutableState.update { it.copy(saving = false, saved = true) }
            } catch (error: Exception) {
                val message = taskFailure(error, "save task")
                mutableState.update { it.copy(saving = false, saveError = message) }
            }
        }
    }
}
