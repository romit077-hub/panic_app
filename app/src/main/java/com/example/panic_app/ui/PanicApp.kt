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
import com.example.panic_app.ui.navigation.PanicDestination
import com.example.panic_app.ui.screens.analytics.AnalyticsScreen
import com.example.panic_app.ui.screens.calendar.CalendarScreen
import com.example.panic_app.ui.screens.dashboard.DashboardScreen
import com.example.panic_app.ui.screens.panic.PanicScreen
import com.example.panic_app.ui.screens.settings.SettingsScreen
import com.example.panic_app.ui.screens.tasks.TasksScreen
import com.example.panic_app.ui.theme.Panic_appTheme

@Composable
fun PanicApp(onThemeChanged: (Boolean) -> Unit = {}) {
    val systemDark = isSystemInDarkTheme()
    // Hoisted temporary preferences survive navigation and configuration recreation.
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val darkTheme = darkOverride ?: systemDark
    SideEffect { onThemeChanged(darkTheme) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var panicAlerts by rememberSaveable { mutableStateOf(true) }
    var intensity by rememberSaveable { mutableStateOf(1) }
    var showAddInfo by rememberSaveable { mutableStateOf(false) }
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

    Panic_appTheme(darkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        if (!primary) IconButton(onClick = { navController.popBackStack() }) {
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
                modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                composable(PanicDestination.Dashboard.route) {
                    DashboardScreen(onTasks = { openTab(PanicDestination.Tasks) }, onPanic = { openTab(PanicDestination.Panic) },
                        onAnalytics = { openDetail(PanicDestination.Analytics) }, onSettings = { openDetail(PanicDestination.Settings) },
                        onAdd = { showAddInfo = true })
                }
                composable(PanicDestination.Tasks.route) { TasksScreen() }
                composable(PanicDestination.Calendar.route) { CalendarScreen() }
                composable(PanicDestination.Panic.route) { PanicScreen() }
                composable(PanicDestination.Analytics.route) { AnalyticsScreen() }
                composable(PanicDestination.Settings.route) {
                    SettingsScreen(darkTheme, notifications, panicAlerts, intensity,
                        onDarkTheme = { darkOverride = it }, onNotifications = { notifications = it },
                        onPanicAlerts = { panicAlerts = it }, onIntensity = { intensity = it })
                }
            }
        }
        if (showAddInfo) AlertDialog(
            onDismissRequest = { showAddInfo = false },
            title = { Text("Task creation is coming next") },
            text = { Text("This foundation uses read-only sample tasks. Adding and saving your own tasks will be available in the next phase.") },
            confirmButton = { TextButton(onClick = { showAddInfo = false; openTab(PanicDestination.Tasks) }) { Text("Explore sample tasks") } },
            dismissButton = { TextButton(onClick = { showAddInfo = false }) { Text("Close") } }
        )
    }
}
