package com.example.panic_app.model

import org.junit.Assert.*
import org.junit.Test

class ThemePreferenceTest {
    @Test fun absentAndUnknownStoredValuesFollowSystem() {
        assertEquals(ThemePreference.SYSTEM, ThemePreference.fromStored(null))
        assertEquals(ThemePreference.SYSTEM, ThemePreference.fromStored("unknown"))
    }
    @Test fun storedValuesRoundTrip() {
        ThemePreference.entries.forEach { assertEquals(it, ThemePreference.fromStored(it.name)) }
    }
    @Test fun systemModeTracksAndroidAppearance() {
        assertTrue(ThemePreference.SYSTEM.useDarkTheme(true))
        assertFalse(ThemePreference.SYSTEM.useDarkTheme(false))
    }
    @Test fun explicitModesOverrideSystem() {
        assertTrue(ThemePreference.DARK.useDarkTheme(false))
        assertFalse(ThemePreference.LIGHT.useDarkTheme(true))
    }
}
