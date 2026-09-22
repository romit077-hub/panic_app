package com.example.panic_app.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.panic_app.model.RiskLevel

data class RiskColors(val foreground: Color, val background: Color)

fun riskColors(level: RiskLevel, dark: Boolean): RiskColors = when (level) {
    RiskLevel.SAFE -> if (dark) RiskColors(Color(0xFF83DEB4), Color(0xFF173C30)) else RiskColors(Color(0xFF126342), Color(0xFFDCF5E8))
    RiskLevel.WARNING -> if (dark) RiskColors(Color(0xFFF7D47E), Color(0xFF453817)) else RiskColors(Color(0xFF735200), Color(0xFFFFF0C2))
    RiskLevel.HIGH -> if (dark) RiskColors(Color(0xFFFFBB91), Color(0xFF4C2C1B)) else RiskColors(Color(0xFF963D0A), Color(0xFFFFE7D7))
    RiskLevel.CRITICAL -> if (dark) RiskColors(Color(0xFFFFAFB6), Color(0xFF4D252C)) else RiskColors(Color(0xFFAD263C), Color(0xFFFFE2E7))
}

val Brand = Color(0xFF4659B8)
val BrandLight = Color(0xFFBBC3FF)
val LightBackground = Color(0xFFF7F8FC)
val DarkBackground = Color(0xFF11131C)
val LightInk = Color(0xFF1B1E2D)
val DarkInk = Color(0xFFE6E7F2)
