package com.example.panic_app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.panic_app.model.TaskPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC, id ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDateMillis ASC, id ASC")
    fun observePending(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueDateMillis ASC, id ASC")
    fun observeCompleted(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Insert suspend fun insert(task: TaskEntity): Long

    // Update editable columns only: a stale form cannot reset completion or creation time.
    @Query("""UPDATE tasks SET title = :title, description = :description, subject = :subject,
        dueDateMillis = :dueDateMillis, estimatedMinutes = :estimatedMinutes, priority = :priority
        WHERE id = :id""")
    suspend fun update(id: Long, title: String, description: String, subject: String,
        dueDateMillis: Long, estimatedMinutes: Int, priority: TaskPriority): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean): Int

    @Query("UPDATE tasks SET isCompleted = NOT isCompleted WHERE id = :id")
    suspend fun toggleCompletion(id: Long): Int
}
