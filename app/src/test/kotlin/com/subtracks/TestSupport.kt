package com.subtracks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking

// Wall-clock budget for the tests that poll asynchronous playback/repository work on a real
// dispatcher. A loaded CI runner can starve those threads for seconds; this only needs to catch a
// real hang, not a slow machine.
const val TEST_TIMEOUT_MS = 30_000L

fun cancelAndJoinBlocking(vararg scopes: CoroutineScope) {
    runBlocking { scopes.forEach { it.coroutineContext[Job]?.cancelAndJoin() } }
}
