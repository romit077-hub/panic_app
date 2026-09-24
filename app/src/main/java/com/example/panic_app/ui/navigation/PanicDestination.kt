package com.example.panic_app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class PanicDestination(val route: String, val title: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Home),
    Tasks("tasks", "Tasks", Icons.AutoMirrored.Filled.List),
    Plan("smart_planner", "Plan", Icons.Default.CheckCircle),
    Calendar("calendar", "Calendar", Icons.Default.DateRange),
    Panic("panic", "Panic", Icons.Default.Warning),
    Analytics("analytics", "Analytics", Icons.Default.Info),
    Settings("settings", "Settings", Icons.Default.Settings),
    AddTask("task/add", "Add Task", Icons.Default.Add),
    EditTask("task/edit/{taskId}", "Edit Task", Icons.Default.Edit);

    fun forTask(id: Long): String {
        require(this == EditTask && id > 0)
        return route.replace("{taskId}", id.toString())
    }

    companion object {
        val tabs = listOf(Dashboard, Tasks, Plan, Calendar, Panic)
        fun fromRoute(route: String?) = entries.firstOrNull { it.route == route } ?: Dashboard
    }
}
