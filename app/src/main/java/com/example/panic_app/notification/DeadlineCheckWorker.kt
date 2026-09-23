package com.example.panic_app.notification

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.panic_app.PanicApplication
import java.io.IOException

class DeadlineCheckWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        (applicationContext as PanicApplication).reminders.check()
        Result.success()
    } catch (error: IOException) {
        Log.w("DeadlineCheck", "Reminder state could not be saved", error)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    } catch (error: SQLiteException) {
        Log.w("DeadlineCheck", "Tasks could not be read", error)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }
}
