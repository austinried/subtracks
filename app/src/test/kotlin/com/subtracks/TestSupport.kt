package com.subtracks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue

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
    assertTrue("Timed out after ${TEST_TIMEOUT_MS}ms $message", predicate())
}
