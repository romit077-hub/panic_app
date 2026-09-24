package com.example.panic_app.data.settings

import android.content.Context
import com.example.panic_app.domain.planner.PlannerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException

class PlannerSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("smart_planner", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(PlannerPreferences.decode(preferences.all["config"] as? String))
    val settings = mutable.asStateFlow()
    suspend fun save(config: PlannerConfig) = withContext(Dispatchers.IO) {
        if (!preferences.edit().putString("config", PlannerPreferences.encode(config)).commit())
            throw IOException("Could not save planner settings. Please try again.")
        mutable.value = config
    }
}
