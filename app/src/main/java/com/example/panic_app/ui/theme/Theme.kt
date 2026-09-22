package com.example.panic_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalPanicDarkTheme = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = Brand, onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E6FF), onPrimaryContainer = Color(0xFF233477),
    secondary = Color(0xFF426A60), background = LightBackground,
    surface = Color.White, onSurface = LightInk, onBackground = LightInk,
    surfaceVariant = Color(0xFFEBEDF5), onSurfaceVariant = Color(0xFF575D72),
    outlineVariant = Color(0xFFDDE0EC)
)
private val DarkColors = darkColorScheme(
    primary = BrandLight, onPrimary = Color(0xFF18245E),
    primaryContainer = Color(0xFF303E7D), onPrimaryContainer = Color(0xFFE0E4FF),
    secondary = Color(0xFFA3D0C1), background = DarkBackground,
    surface = Color(0xFF1B1E2B), onSurface = DarkInk, onBackground = DarkInk,
    surfaceVariant = Color(0xFF292D40), onSurfaceVariant = Color(0xFFBFC3D7),
    outlineVariant = Color(0xFF3C4055)
)

@Composable
fun Panic_appTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPanicDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography, content = content)
    }
}
