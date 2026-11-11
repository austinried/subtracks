package com.subtracks.ui.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalNetworkGateTest {
    @Test
    fun runsTheActionWhenThePermissionIsNotNeeded() =
        runTest {
            var checked = 0
            val gate =
                gate(requires = {
                    checked++
                    false
                })
            var ran = false
            gate.run("http://10.0.0.5", onGranted = { ran = true })
            advanceUntilIdle()
            assertEquals(1, checked)
            assertTrue(ran)
        }

    @Test
    fun runsTheNotNeededCallbackForANullAddress() =
        runTest {
            val gate = gate(requires = { error("should not check a null address") })
            var notNeeded = false
            gate.run(null, onGranted = { error("should not run") }, onNotNeeded = { notNeeded = true })
            advanceUntilIdle()
            assertTrue(notNeeded)
        }

    @Test
    fun runsTheActionOnGrant() =
        runTest {
            var requested: String? = null
            val gate =
                gate(
                    requires = { true },
                    request = { address, onGranted, _ ->
                        requested = address
                        onGranted()
                    },
                )
            var ran = false
            gate.run("http://10.0.0.5", onGranted = { ran = true })
            advanceUntilIdle()
            assertEquals("http://10.0.0.5", requested)
            assertTrue(ran)
        }

    @Test
    fun runsTheDeniedCallbackOnDenial() =
        runTest {
            val gate = gate(requires = { true }, request = { _, _, onDenied -> onDenied() })
            var denied = false
            gate.run("http://10.0.0.5", onGranted = { error("should not run") }, onDenied = { denied = true })
            advanceUntilIdle()
            assertTrue(denied)
        }

    @Test
    fun skipsTheAutomaticRequestForADeniedAddress() =
        runTest {
            var requested = false
            val gate = gate(requires = { true }, denied = { true }, request = { _, _, _ -> requested = true })
            var notNeeded = false
            gate.run("http://10.0.0.5", onGranted = { error("should not run") }, onNotNeeded = { notNeeded = true }, automatic = true)
            advanceUntilIdle()
            assertFalse(requested)
            assertTrue(notNeeded)
        }

    @Test
    fun cancellingDropsTheResult() =
        runTest {
            val pending = CompletableDeferred<Boolean>()
            var cleared = 0
            val gate = gate(requires = { pending.await() }, clear = { cleared++ })
            var ran = false
            gate.run("http://10.0.0.5", onGranted = { ran = true })
            advanceUntilIdle()
            gate.cancel()
            pending.complete(true)
            advanceUntilIdle()
            assertFalse(ran)
            assertEquals(2, cleared)
        }

    @Test
    fun ignoresAnEarlierCheckWhenANewAddressStarts() =
        runTest {
            val first = CompletableDeferred<Boolean>()
            val second = CompletableDeferred<Boolean>()
            var requested: String? = null
            val gate =
                gate(
                    requires = { address -> if (address == "http://a") first.await() else second.await() },
                    request = { address, onGranted, _ ->
                        requested = address
                        onGranted()
                    },
                )
            gate.run("http://a", onGranted = {})
            advanceUntilIdle()
            gate.run("http://b", onGranted = {})
            advanceUntilIdle()
            second.complete(true)
            advanceUntilIdle()
            first.complete(true)
            advanceUntilIdle()
            assertEquals("http://b", requested)
        }

    @Test
    fun cancellingTheOwningScopeDropsTheResult() =
        runTest {
            val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
            val pending = CompletableDeferred<Boolean>()
            val gate =
                LocalNetworkGate(
                    scope = scope,
                    requiresPermission = { pending.await() },
                    wasDenied = { false },
                    requestPermission = { _, onGranted, _ -> onGranted() },
                    clearPrompt = {},
                )
            var ran = false
            gate.run("http://10.0.0.5", onGranted = { ran = true })
            advanceUntilIdle()
            scope.cancel()
            pending.complete(true)
            advanceUntilIdle()
            assertFalse(ran)
        }

    private fun TestScope.gate(
        requires: suspend (String) -> Boolean,
        denied: (String) -> Boolean = { false },
        request: (String, () -> Unit, () -> Unit) -> Unit = { _, onGranted, _ -> onGranted() },
        clear: () -> Unit = {},
    ) = LocalNetworkGate(
        scope = this,
        requiresPermission = requires,
        wasDenied = denied,
        requestPermission = request,
        clearPrompt = clear,
    )
}
