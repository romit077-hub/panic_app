package com.example.panic_app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.example.panic_app.domain.risk.RiskResult
import com.example.panic_app.ui.theme.*

@Composable
fun PanicProgress(value: Float, modifier: Modifier = Modifier) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(if (entered) value.coerceIn(0f, 1f) else 0f,
        tween(PanicDesign.motionMillis), label = "Progress")
    LinearProgressIndicator(progress = { progress }, modifier = modifier.fillMaxWidth())
}

/** Displays one existing task's risk. Never an invented aggregate score. */
@Composable
fun RiskGauge(risk: RiskResult) {
    val colors = riskColors(risk.level, LocalPanicDarkTheme.current)
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val progress by animateFloatAsState(if (entered) risk.score / 100f else 0f,
        tween(320), label = "Deadline risk")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PanicDesign.contentGap)) {
        Box(Modifier.size(100.dp).clearAndSetSemantics { contentDescription = "Task risk ${risk.score} out of 100, ${risk.level.name}" }, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(),
                color = colors.foreground, trackColor = colors.background, strokeWidth = 7.dp)
            Text(risk.score.toString(), style = MaterialTheme.typography.headlineMedium)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Highest task risk", style = MaterialTheme.typography.labelLarge)
            RiskBadge(risk.level)
            Text("Urgency, not a probability", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
