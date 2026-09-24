package com.example.panic_app

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.panic_app.notification.PanicNotificationManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import com.example.panic_app.ui.PanicApp

class MainActivity : ComponentActivity() {
    private var openPanic by mutableStateOf(false)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openPanic = intent.action == PanicNotificationManager.ACTION_PANIC
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openPanic = intent.action == PanicNotificationManager.ACTION_PANIC
        enableEdgeToEdge()
        setContent {
            val app = application as PanicApplication
            PanicApp(repository = app.taskRepository, reminders = app.reminders, plannerSettings = app.plannerSettings,
                openPanic = openPanic, onPanicOpened = { openPanic = false; intent.action = null },
                onThemeChanged = { dark ->
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                )
            })
        }
    }
}
