package com.example.panic_app.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panic_app.data.local.TaskEntity
import com.example.panic_app.data.repository.*
import com.example.panic_app.data.settings.*
import com.example.panic_app.domain.planner.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.ZoneId

data class PlannerUiState(
    val loading: Boolean = true,
    val generating: Boolean = false,
    val saving: Boolean = false,
    val tasks: List<TaskEntity> = emptyList(),
    val config: PlannerConfig = PlannerPreferences.defaults,
    val plan: StudyPlan? = null,
    val error: String? = null,
    val zone: ZoneId = ZoneId.systemDefault()
)

class PlannerViewModel(private val repository: TaskRepository, private val store: PlannerSettingsStore,
    private val clock: () -> Long = System::currentTimeMillis) : ViewModel() {
    private val mutable = MutableStateFlow(PlannerUiState())
    val state = mutable.asStateFlow()
    private var observation: Job? = null
    private var generation: Job? = null
    private var requested = false
    init { retry() }
    fun retry() {
        observation?.cancel()
        generation?.cancel()
        mutable.update { it.copy(loading = true, generating = false, plan = null, error = null) }
        observation = viewModelScope.launch {
            try {
                combine(repository.observeAll(), store.settings) { tasks, config -> tasks to config }
                    .collect { (tasks, config) ->
                        mutable.update { it.copy(tasks = tasks, config = config, loading = false, error = null, plan = null) }
                        if (requested) generate()
                    }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(loading = false, error = "Could not load planner data. Please retry.") } }
        }
    }
    fun generate() {
        if (mutable.value.loading) return
        requested = true
        generation?.cancel()
        val snapshot = mutable.value
        val now = clock()
        val zone = ZoneId.systemDefault()
        mutable.update { it.copy(generating = true, error = null, plan = null, zone = zone) }
        generation = viewModelScope.launch {
            try {
                val plan = withContext(Dispatchers.Default) { snapshot.tasks.generateStudyPlan(snapshot.config, now, zone) }
                mutable.update { it.copy(plan = plan, generating = false) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(generating = false, error = "Could not build the plan. Review settings and try again.") } }
        }
    }
    fun save(config: PlannerConfig) {
        if (mutable.value.saving) return
        mutable.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try { store.save(config) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.update { it.copy(error = "Settings were not saved. Please try again.") } }
            finally { mutable.update { it.copy(saving = false) } }
        }
    }
}
