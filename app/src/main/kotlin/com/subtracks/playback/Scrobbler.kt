package com.subtracks.playback

import com.subtracks.data.source.ServerActionSink
import com.subtracks.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface Scrobble {
    val songId: String

    data class NowPlaying(
        override val songId: String,
    ) : Scrobble

    data class Submission(
        override val songId: String,
        val time: Long,
    ) : Scrobble
}

class ScrobblePolicy(
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var songId: String? = null
    private var lastItemId: String? = null
    private var durationMs = 0L
    private var startedAt = 0L
    private var playedMs = 0L
    private var lastPositionMs = 0L
    private var nowPlayingSent = false
    private var submitted = false

    fun reset() {
        songId = null
        lastItemId = null
        playedMs = 0
        lastPositionMs = 0
        nowPlayingSent = false
        submitted = false
    }

    fun advance(
        item: QueueItem?,
        durationMs: Long,
        isPlaying: Boolean,
        positionMs: Long,
    ): Scrobble? {
        if (durationMs > 0) this.durationMs = durationMs
        val previousPosition = lastPositionMs
        val restarted =
            item?.id != songId ||
                (item != null && item.id == lastItemId && positionMs < previousPosition && positionMs <= RESTART_POSITION_MS)
        if (restarted) {
            songId = item?.id
            lastItemId = null
            playedMs = 0
            lastPositionMs = positionMs
            nowPlayingSent = false
            submitted = false
            if (item == null || !isPlaying) return null
            nowPlayingSent = true
            startedAt = now()
            return Scrobble.NowPlaying(item.id)
        }
        lastPositionMs = positionMs
        lastItemId = item?.id
        val id = songId ?: return null
        if (!isPlaying) return null
        if (!nowPlayingSent) {
            nowPlayingSent = true
            startedAt = now()
            return Scrobble.NowPlaying(id)
        }
        playedMs += (positionMs - previousPosition).coerceIn(0, MAX_STEP_MS)
        if (submitted || playedMs < thresholdMs()) return null
        submitted = true
        return Scrobble.Submission(id, startedAt)
    }

    private fun thresholdMs(): Long {
        if (durationMs in 1 until MIN_DURATION_MS) return Long.MAX_VALUE
        if (durationMs <= 0) return MAX_THRESHOLD_MS
        return minOf(durationMs / 2, MAX_THRESHOLD_MS)
    }

    private companion object {
        const val MIN_DURATION_MS = 30_000L
        const val MAX_THRESHOLD_MS = 4 * 60_000L
        const val MAX_STEP_MS = 2_000L
        const val RESTART_POSITION_MS = 3_000L
    }
}

class Scrobbler(
    private val sink: ServerActionSink,
    private val enabled: Flow<Boolean>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val policy: ScrobblePolicy = ScrobblePolicy(),
    private val recordPlay: suspend (songId: String, at: Long) -> Unit = { _, _ -> },
) {
    private val submitLock = Mutex()

    fun attach(
        state: StateFlow<PlaybackState>,
        positionMs: StateFlow<Long>,
    ) {
        scope.launch {
            combine(state, positionMs, enabled, ::Triple)
                .collect { (current, position, on) ->
                    if (!on) {
                        policy.reset()
                        return@collect
                    }
                    val durationMs = current.item?.durationMs?.takeIf { it > 0 } ?: current.durationMs
                    val event = policy.advance(current.item, durationMs, current.isPlaying, position) ?: return@collect
                    scope.launch { submitLock.withLock { submit(event) } }
                }
        }
    }

    private suspend fun submit(event: Scrobble) {
        when (event) {
            is Scrobble.NowPlaying -> {
                Log.i("scrobble", "now playing ${event.songId}")
                attempt("now playing ${event.songId}") { sink.nowPlaying(event.songId) }
            }

            is Scrobble.Submission -> {
                Log.i("scrobble", "submitting ${event.songId}")
                attempt("record play ${event.songId}") { recordPlay(event.songId, event.time / 1000) }
                attempt("scrobble ${event.songId}") { sink.scrobble(event.songId, event.time) }
            }
        }
    }

    private suspend fun attempt(
        label: String,
        block: suspend () -> Unit,
    ) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            Log.w("scrobble", "$label failed", failure)
        }
    }
}
