package com.example.panic_app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.example.panic_app.data.repository.TaskRepository
import com.example.panic_app.ui.screens.tasks.TasksViewModel
import com.example.panic_app.ui.screens.addedittask.TaskEditorViewModel
import com.example.panic_app.ui.screens.addedittask.TaskEditorScreen
import com.example.panic_app.ui.navigation.PanicDestination
import com.example.panic_app.ui.screens.analytics.AnalyticsScreen
import com.example.panic_app.ui.screens.calendar.CalendarScreen
import com.example.panic_app.ui.screens.dashboard.DashboardScreen
import com.example.panic_app.ui.screens.panic.PanicScreen
import com.example.panic_app.ui.screens.settings.ReminderSettingsRoute
import com.example.panic_app.notification.DeadlineReminderController
import com.example.panic_app.ui.screens.tasks.TasksScreen
import com.example.panic_app.ui.theme.Panic_appTheme

@Composable
fun PanicApp(repository: TaskRepository, reminders: DeadlineReminderController,
    openPanic: Boolean = false, onPanicOpened: () -> Unit = {}, onThemeChanged: (Boolean) -> Unit = {}) {
    val tasksViewModel: TasksViewModel = viewModel(factory = remember(repository) {
        viewModelFactory { initializer { TasksViewModel(repository) } }
    })
    val tasksState by tasksViewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, tasksViewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Runs immediately on foreground/resume, then only while visible. Background notification checks have their own WorkManager schedule.
            while (isActive) {
                tasksViewModel.refreshTime()
                delay(TasksViewModel.RISK_REFRESH_INTERVAL_MILLIS)
            }
        }
    }
    val systemDark = isSystemInDarkTheme()
    // Hoisted temporary preferences survive navigation and configuration recreation.
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val darkTheme = darkOverride ?: systemDark
    SideEffect { onThemeChanged(darkTheme) }
    var editorSaving by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val current = PanicDestination.fromRoute(entry?.destination?.route)
    val primary = current in PanicDestination.tabs

    fun openTab(destination: PanicDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun openDetail(destination: PanicDestination) {
        navController.navigate(destination.route) { launchSingleTop = true }
    }

    LaunchedEffect(openPanic) {
        if (openPanic) {
            openTab(PanicDestination.Panic)
            onPanicOpened()
        }
    }
    Panic_appTheme(darkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (!primary) IconButton(enabled = !editorSaving, onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                        Column(Modifier.padding(start = 8.dp).weight(1f)) {
                            Text(if (primary) "PANIC" else current.title, style = MaterialTheme.typography.titleLarge)
                            if (primary) Text("Deadline Enforcer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (primary) IconButton(onClick = { openDetail(PanicDestination.Settings) }) {
                            Icon(PanicDestination.Settings.icon, "Settings")
                        }
                    }
                }
            },
            bottomBar = {
                if (primary) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    PanicDestination.tabs.forEach { tab ->
                        NavigationBarItem(selected = current == tab, onClick = { openTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) }, label = { Text(tab.title) })
                    }
                }
            }
        ) { padding ->
            NavHost(navController = navController, startDestination = PanicDestination.Dashboard.route,
                modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
                composable(PanicDestination.Dashboard.route) {
                    DashboardScreen(state = tasksState, onRetry = tasksViewModel::retry,
                        onEdit = { openEdit(navController, it) }, onTasks = { openTab(PanicDestination.Tasks) }, onPanic = { openTab(PanicDestination.Panic) },
                        onAnalytics = { openDetail(PanicDestination.Analytics) }, onSettings = { openDetail(PanicDestination.Settings) },
                        onAdd = { openDetail(PanicDestination.AddTask) })
                }
                composable(PanicDestination.Tasks.route) {
                    TasksScreen(tasksState, onAdd = { openDetail(PanicDestination.AddTask) },
                        onEdit = { openEdit(navController, it) }, onDelete = tasksViewModel::delete,
                        onToggle = tasksViewModel::toggle, onRetry = tasksViewModel::retry,
                        onDismissError = tasksViewModel::clearActionError)
                }
                composable(PanicDestination.AddTask.route) {
                    TaskEditorRoute(repository, null, onFinished = { navController.popBackStack() }, onSaving = { editorSaving = it })
                }
                composable(PanicDestination.EditTask.route,
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType })) { backStackEntry ->
                    TaskEditorRoute(repository, requireNotNull(backStackEntry.arguments).getLong("taskId"),
                        onFinished = { navController.popBackStack() }, onSaving = { editorSaving = it })
                }
                composable(PanicDestination.Calendar.route) { CalendarScreen(tasksState, onEdit = { openEdit(navController, it) }, onRetry = tasksViewModel::retry) }
                composable(PanicDestination.Panic.route) { PanicScreen(tasksState, onEdit = { openEdit(navController, it) }, onTasks = { openTab(PanicDestination.Tasks) }, onRetry = tasksViewModel::retry) }
                composable(PanicDestination.Analytics.route) { AnalyticsScreen() }
                composable(PanicDestination.Settings.route) {
                    ReminderSettingsRoute(reminders, darkTheme, onDarkTheme = { darkOverride = it })
                }
            }
        }
    }
}

private fun openEdit(navController: androidx.navigation.NavHostController, id: Long) {
    navController.navigate(PanicDestination.EditTask.forTask(id)) { launchSingleTop = true }
}

@Composable
private fun TaskEditorRoute(repository: TaskRepository, taskId: Long?, onFinished: () -> Unit, onSaving: (Boolean) -> Unit) {
    val factory = remember(repository, taskId) {
        viewModelFactory { initializer { TaskEditorViewModel(repository, createSavedStateHandle(), taskId) } }
    }
    val editor: TaskEditorViewModel = viewModel(factory = factory)
    val state by editor.state.collectAsStateWithLifecycle()
    val finish by rememberUpdatedState(onFinished)
    SideEffect { onSaving(state.saving) }
    DisposableEffect(Unit) { onDispose { onSaving(false) } }
    LaunchedEffect(state.saved) { if (state.saved) finish() }
    TaskEditorScreen(state, taskId != null, onChange = editor::change, onDeadline = editor::changeDeadline,
        onDateError = editor::dateError, onSave = editor::save, onCancel = onFinished, onRetry = editor::load)
}
