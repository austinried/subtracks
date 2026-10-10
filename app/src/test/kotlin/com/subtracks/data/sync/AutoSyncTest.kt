package com.subtracks.data.sync

import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.DEFAULT_SYNC_INTERVAL_MINUTES
import com.subtracks.data.prefs.SyncMode
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class AutoSyncTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
    }

    @Test
    fun decidesWhetherASyncIsDue() {
        assertTrue(shouldSync())
        assertTrue(shouldSync(mode = SyncMode.WifiOnly))
        assertTrue(shouldSync(mode = SyncMode.Always, network = NetworkMode.Mobile))
        assertTrue(shouldSync(lastSyncAt = INTERVAL_MS * 2, now = INTERVAL_MS))

        assertFalse(shouldSync(mode = SyncMode.Off))
        assertFalse(shouldSync(mode = SyncMode.WifiOnly, network = NetworkMode.Mobile))
        assertFalse(shouldSync(lastSyncAt = INTERVAL_MS - 1, now = INTERVAL_MS))
        assertFalse(shouldSync(offline = true))
        assertFalse(shouldSync(hasSource = false))
    }

    @Test
    fun syncsWhenTheAppIsActiveAndDue() {
        val preferences = fakeUserPreferences()
        runBlocking {
            preferences.setSyncMode(SyncMode.WifiOnly)
            preferences.setLastSyncAt(0L)
        }
        val requested = AtomicBoolean(false)
        val autoSync = autoSync(preferences, requestSync = { requested.set(true) })

        autoSync.setForeground(true)

        awaitUntil("auto sync requested") { requested.get() }
        autoSync.setForeground(false)
    }

    @Test
    fun doesNotSyncWhenOff() {
        val preferences = fakeUserPreferences()
        runBlocking {
            preferences.setSyncMode(SyncMode.Off)
            preferences.setLastSyncAt(0L)
        }
        val requested = AtomicBoolean(false)
        val autoSync = autoSync(preferences, requestSync = { requested.set(true) })

        autoSync.setForeground(true)
        runBlocking { delay(300) }
        autoSync.setForeground(false)

        assertFalse(requested.get())
    }

    @Test
    fun stopsSyncingWhenBackgrounded() {
        val preferences = fakeUserPreferences()
        runBlocking {
            preferences.setSyncMode(SyncMode.Always)
            preferences.setLastSyncAt(0L)
        }
        val count = AtomicInteger(0)
        val autoSync =
            AutoSync(
                preferences = preferences,
                networkMode = flowOf(NetworkMode.Wifi),
                hasSource = { true },
                requestSync = { count.incrementAndGet() },
                scope = scope,
                now = { INTERVAL_MS + 1 },
                maxPollMs = 50L,
            )

        autoSync.setForeground(true)
        awaitUntil("first sync") { count.get() >= 1 }
        autoSync.setForeground(false)

        runBlocking { delay(200) }
        val afterStop = count.get()
        runBlocking { delay(300) }

        assertEquals(afterStop, count.get())
    }

    private fun autoSync(
        preferences: UserPreferences,
        requestSync: () -> Unit,
        now: () -> Long = { INTERVAL_MS + 1 },
    ): AutoSync =
        AutoSync(
            preferences = preferences,
            networkMode = flowOf(NetworkMode.Wifi),
            hasSource = { true },
            requestSync = requestSync,
            scope = scope,
            now = now,
            maxPollMs = 10_000L,
        )

    private fun shouldSync(
        mode: SyncMode = SyncMode.WifiOnly,
        network: NetworkMode = NetworkMode.Wifi,
        offline: Boolean = false,
        hasSource: Boolean = true,
        lastSyncAt: Long = 0L,
        now: Long = INTERVAL_MS,
        intervalMs: Long = INTERVAL_MS,
    ): Boolean =
        shouldAutoSync(
            mode = mode,
            network = network,
            offline = offline,
            hasSource = hasSource,
            lastSyncAt = lastSyncAt,
            now = now,
            intervalMs = intervalMs,
        )

    private companion object {
        val INTERVAL_MS = DEFAULT_SYNC_INTERVAL_MINUTES * 60_000L
    }
}
