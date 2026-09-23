package com.example.panic_app.data.repository

import com.example.panic_app.data.local.TaskDao
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.model.TaskDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock

class TaskMissingException : IllegalStateException("This task no longer exists.")

class TaskRepository(private val dao: TaskDao, private val gate: TaskMutationGate = TaskMutationGate()) {
    fun observeAll() = dao.observeAll()
    fun observePending() = dao.observePending()
    fun observeCompleted() = dao.observeCompleted()
    suspend fun getTask(id: Long) = dao.getById(id)

    suspend fun addTask(draft: TaskDraft): Long {
        require(draft.errors().isEmpty()) { "Invalid task draft" }
        return mutate {
            val id = dao.insert(TaskEntity(title = draft.title.trim(), description = draft.description.trim(),
                subject = draft.subject.trim(), dueDateMillis = draft.dueDateMillis,
                estimatedMinutes = draft.estimatedMinutes.toInt(), priority = draft.priority))
            gate.onChanged(id)
            id
        }
    }
    suspend fun updateTask(id: Long, draft: TaskDraft) {
        require(draft.errors().isEmpty()) { "Invalid task draft" }
        mutate {
            if (dao.update(id, draft.title.trim(), draft.description.trim(), draft.subject.trim(),
                    draft.dueDateMillis, draft.estimatedMinutes.toInt(), draft.priority) == 0) throw TaskMissingException()
            gate.onChanged(id)
        }
    }
    suspend fun deleteTask(id: Long) = mutate {
        if (dao.delete(id) == 0) throw TaskMissingException()
        gate.onChanged(id)
    }
    suspend fun toggleCompletion(id: Long) = mutate {
        if (dao.toggleCompletion(id) == 0) throw TaskMissingException()
        gate.onChanged(id)
    }
    private suspend fun <T> mutate(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        // Once a Room write starts, complete notification cancellation before releasing the gate.
        gate.mutex.withLock { withContext(NonCancellable) { block() } }
    }
}
