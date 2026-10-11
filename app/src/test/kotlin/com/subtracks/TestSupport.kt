package com.subtracks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.fail

const val TEST_TIMEOUT_MS = 30_000L

fun cancelAndJoinBlocking(vararg scopes: CoroutineScope) {
    runBlocking { scopes.forEach { it.coroutineContext[Job]?.cancelAndJoin() } }
}

fun awaitUntil(
    message: String,
    predicate: () -> Boolean,
) {
    val deadline = System.nanoTime() + TEST_TIMEOUT_MS * 1_000_000
    while (!predicate() && System.nanoTime() < deadline) Thread.sleep(10)
    if (predicate()) return
    // A wall-clock wait that expires means something stalled rather than ran slow: the suite
    // finishes in milliseconds per test. Print every thread's stack in the failure message so the
    // stall is diagnosable from the CI log, which shows exception messages but not the test XML.
    fail("Timed out after ${TEST_TIMEOUT_MS}ms $message\n\n${threadDump()}")
}

private fun threadDump(): String =
    Thread.getAllStackTraces().entries.joinToString("\n\n") { (thread, frames) ->
        "${thread.name} [${thread.state}]\n" + frames.joinToString("\n") { "    at $it" }
    }
