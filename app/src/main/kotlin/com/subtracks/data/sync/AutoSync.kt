package com.subtracks.data.sync

import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.SyncMode
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val AUTO_SYNC_MAX_POLL_MS = 15L * 60 * 1000

internal fun shouldAutoSync(
    mode: SyncMode,
    network: NetworkMode,
    offline: Boolean,
    hasSource: Boolean,
    lastSyncAt: Long,
    now: Long,
    intervalMs: Long,
): Boolean {
    val allowed =
        when (mode) {
            SyncMode.Off -> false
            SyncMode.WifiOnly -> network == NetworkMode.Wifi
            SyncMode.Always -> true
        }
    return allowed && !offline && hasSource && (lastSyncAt > now || now - lastSyncAt >= intervalMs)
}

class AutoSync(
    private val preferences: UserPreferences,
    private val networkMode: Flow<NetworkMode>,
    private val hasSource: suspend () -> Boolean,
    private val requestSync: () -> Unit,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
    private val maxPollMs: Long = AUTO_SYNC_MAX_POLL_MS,
) {
    @Volatile
    private var lastNetwork: NetworkMode? = null
    private var loop: Job? = null

    init {
        scope.launch { networkMode.collect { lastNetwork = it } }
    }

    fun setForeground(active: Boolean) {
        if (active) start() else stop()
    }

    private fun start() {
        if (loop?.isActive == true) return
        loop =
            scope.launch {
                while (isActive) {
                    val intervalMs =
                        try {
                            checkAndRequest()
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (failure: Exception) {
                            Log.w("sync", "auto-sync check failed", failure)
                            maxPollMs
                        }
                    delay(intervalMs.coerceAtMost(maxPollMs))
                }
            }
    }

    private fun stop() {
        loop?.cancel()
        loop = null
    }

    private suspend fun checkAndRequest(): Long {
        val mode = preferences.syncMode().first()
        val network = lastNetwork ?: networkMode.first()
        val offline = preferences.offlineMode().first()
        val hasSource = hasSource()
        val lastSyncAt = preferences.lastSyncAt().first()
        val intervalMs = preferences.syncIntervalMinutes().first() * 60_000L
        val time = now()
        val due =
            shouldAutoSync(
                mode = mode,
                network = network,
                offline = offline,
                hasSource = hasSource,
                lastSyncAt = lastSyncAt,
                now = time,
                intervalMs = intervalMs,
            )
        Log.d(
            "sync",
            "auto-sync due=$due (mode=$mode, network=$network, offline=$offline, hasSource=$hasSource, " +
                "sinceLast=${time - lastSyncAt}ms, interval=${intervalMs}ms)",
        )
        if (due && currentCoroutineContext().isActive) requestSync()
        return intervalMs
    }
}
