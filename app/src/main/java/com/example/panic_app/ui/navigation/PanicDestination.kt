package com.example.panic_app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class PanicDestination(val route: String, val title: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Home),
    Tasks("tasks", "Tasks", Icons.AutoMirrored.Filled.List),
    Calendar("calendar", "Calendar", Icons.Default.DateRange),
    Panic("panic", "Panic", Icons.Default.Warning),
    Analytics("analytics", "Analytics", Icons.Default.Info),
    Settings("settings", "Settings", Icons.Default.Settings);

    companion object {
        val tabs = listOf(Dashboard, Tasks, Calendar, Panic)
        fun fromRoute(route: String?) = entries.firstOrNull { it.route == route } ?: Dashboard
    }
}
