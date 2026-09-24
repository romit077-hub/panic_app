package com.example.panic_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panic_app.domain.risk.RiskResult
import com.example.panic_app.domain.risk.formatRemainingTime
import com.example.panic_app.ui.theme.LocalPanicDarkTheme
import com.example.panic_app.ui.theme.riskColors

@Composable
fun RiskSummary(risk: RiskResult, expanded: Boolean = false) {
    if (!risk.isActive) return
    val colors = riskColors(risk.level, LocalPanicDarkTheme.current)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RiskBadge(risk.level)
            Text("${risk.score} / 100", style = MaterialTheme.typography.labelLarge, color = colors.foreground)
        }
        Text(formatRemainingTime(risk.remainingMillis), style = MaterialTheme.typography.bodyMedium,
            color = if (risk.isOverdue) colors.foreground else MaterialTheme.colorScheme.onSurfaceVariant)
        if (expanded) {
            PanicProgress(risk.score / 100f)
            Text("Why this score", style = MaterialTheme.typography.labelLarge)
            Text(risk.reason, style = MaterialTheme.typography.bodyMedium)
            Text("Recommended action", style = MaterialTheme.typography.labelLarge)
            Text(risk.recommendedAction, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
