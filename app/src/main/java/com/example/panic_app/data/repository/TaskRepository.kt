package com.example.panic_app.data.repository

import com.example.panic_app.data.local.TaskDao
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.model.TaskDraft

class TaskMissingException : IllegalStateException("This task no longer exists.")

class TaskRepository(private val dao: TaskDao) {
    fun observeAll() = dao.observeAll()
    fun observePending() = dao.observePending()
    fun observeCompleted() = dao.observeCompleted()
    suspend fun getTask(id: Long) = dao.getById(id)

    suspend fun addTask(draft: TaskDraft): Long {
        require(draft.errors().isEmpty()) { "Invalid task draft" }
        return dao.insert(TaskEntity(title = draft.title.trim(), description = draft.description.trim(),
            subject = draft.subject.trim(), dueDateMillis = draft.dueDateMillis,
            estimatedMinutes = draft.estimatedMinutes.toInt(), priority = draft.priority))
    }
    suspend fun updateTask(id: Long, draft: TaskDraft) {
        require(draft.errors().isEmpty()) { "Invalid task draft" }
        if (dao.update(id, draft.title.trim(), draft.description.trim(), draft.subject.trim(),
                draft.dueDateMillis, draft.estimatedMinutes.toInt(), draft.priority) == 0) throw TaskMissingException()
    }
    suspend fun deleteTask(id: Long) { if (dao.delete(id) == 0) throw TaskMissingException() }
    suspend fun toggleCompletion(id: Long) { if (dao.toggleCompletion(id) == 0) throw TaskMissingException() }
}
