package com.example.panic_app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.panic_app.data.local.PanicDatabase
import com.example.panic_app.data.repository.TaskRepository
import com.example.panic_app.model.TaskDraft
import com.example.panic_app.model.TaskPriority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "panic-phase3-test-${System.nanoTime()}.db"
    private lateinit var database: PanicDatabase
    private lateinit var repository: TaskRepository
    private val draft = TaskDraft(title = "  Assignment  ", subject = " CN ", dueDateMillis = 1800000000000)
    private fun open() {
        database = Room.databaseBuilder(context, PanicDatabase::class.java, databaseName).build()
        repository = TaskRepository(database.taskDao())
    }
    @Before fun setup() { open() }
    @After fun cleanup() { database.close(); context.deleteDatabase(databaseName) }

    @Test fun completedTaskSurvivesEditAndDatabaseReopen() = runBlocking {
        val id = repository.addTask(draft)
        val original = requireNotNull(repository.getTask(id))
        assertEquals("Assignment", original.title)
        assertEquals("CN", original.subject)
        repository.toggleCompletion(id)
        repository.updateTask(id, draft.copy(title = "Updated", priority = TaskPriority.HIGH))
        database.close(); open()
        val restored = requireNotNull(repository.getTask(id))
        assertEquals("Updated", restored.title)
        assertEquals(TaskPriority.HIGH, restored.priority)
        assertTrue(restored.isCompleted)
        assertEquals(original.createdAt, restored.createdAt)
        repository.toggleCompletion(id)
        assertFalse(requireNotNull(repository.getTask(id)).isCompleted)
        repository.deleteTask(id)
        assertNull(repository.getTask(id))
    }

    @Test fun observedFiltersAndDeadlineOrderReflectWrites() = runBlocking {
        val later = repository.addTask(draft.copy(dueDateMillis = 1800000001000))
        val earlier = repository.addTask(draft.copy(dueDateMillis = 1800000000000))
        withTimeout(5000) {
            assertEquals(listOf(earlier, later), repository.observePending().first().map { it.id })
            repository.toggleCompletion(earlier)
            assertEquals(listOf(later), repository.observePending().first().map { it.id })
            assertEquals(listOf(earlier), repository.observeCompleted().first().map { it.id })
            assertEquals(2, repository.observeAll().first().size)
        }
    }

    @Test fun invalidTaskDoesNotInsertAndDefaultDatabaseIsEmpty() = runBlocking {
        assertTrue(repository.observeAll().first().isEmpty())
        try {
            repository.addTask(draft.copy(title = " "))
            fail("Blank title must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertTrue(repository.observeAll().first().isEmpty())
    }
}
