package com.example.panic_app.ui

import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.panic_app.data.repository.TaskMissingException
import java.io.IOException
import kotlinx.coroutines.CancellationException

// These catches exist only at asynchronous UI boundaries; failures remain visible.
internal fun taskFailure(error: Exception, action: String): String {
    if (error is CancellationException) throw error
    return when (error) {
        is TaskMissingException -> "This task no longer exists. Return to Tasks and refresh."
        is SQLiteException, is IOException, is IllegalStateException -> {
            Log.e("PANIC", "Could not $action", error)
            "Could not $action. Please try again. Your saved tasks have not been replaced."
        }
        else -> throw error
    }
}
