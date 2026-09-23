package com.example.panic_app.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteException
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panic_app.notification.DeadlineReminderController
import com.example.panic_app.notification.ReminderIntensity
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun ReminderSettingsRoute(controller: DeadlineReminderController, darkTheme: Boolean, onDarkTheme: (Boolean) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferences by controller.settings.collectAsStateWithLifecycle()
    var status by remember { mutableStateOf(controller.notifications.status()) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        status = controller.notifications.status()
    }
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) status = controller.notifications.status()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    fun perform(action: suspend () -> String) {
        if (busy) return
        scope.launch {
            busy = true
            feedback = null
            try { feedback = action() }
            catch (error: IOException) { feedback = error.message ?: "Could not save reminder settings. Try again." }
            catch (_: SQLiteException) { feedback = "Could not read tasks. Try again." }
            finally { busy = false }
        }
    }
    SettingsScreen(darkTheme, preferences.enabled, preferences.panicAlerts, preferences.intensity.ordinal,
        onDarkTheme = onDarkTheme,
        onNotifications = { enabled -> perform { controller.updateSettings { it.copy(enabled = enabled) }; "Reminder preference saved." } },
        onPanicAlerts = { enabled -> perform { controller.updateSettings { it.copy(panicAlerts = enabled) }; "Panic alert preference saved." } },
        onIntensity = { index -> perform { controller.updateSettings { it.copy(intensity = ReminderIntensity.entries[index]) }; "Intensity saved." } },
        status = status, feedback = feedback, busy = busy,
        canRequestPermission = Build.VERSION.SDK_INT >= 33 && !controller.notifications.available(),
        onPermission = { if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onSystemSettings = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) },
        debug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
        onCheckNow = { perform { controller.check() } })
}
