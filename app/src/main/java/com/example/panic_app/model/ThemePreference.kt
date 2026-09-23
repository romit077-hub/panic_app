package com.example.panic_app.model

enum class ThemePreference(val label: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark");
    fun useDarkTheme(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }
    companion object {
        fun fromStored(value: String?): ThemePreference = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
