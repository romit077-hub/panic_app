package com.example.panic_app.data.repository

import kotlinx.coroutines.sync.Mutex

/** One process-wide gate shared by task writes and notification decisions. */
class TaskMutationGate {
    val mutex = Mutex()
    var onChanged: (id: Long) -> Unit = {}
}
