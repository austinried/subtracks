package com.subtracks.playback

import com.subtracks.data.source.ServerActionSink
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
    private var durationMs = 0L
    private var startedAt = 0L
    private var playedMs = 0L
    private var lastPositionMs = 0L
    private var nowPlayingSent = false
    private var submitted = false

    fun reset() {
        songId = null
        playedMs = 0
        lastPositionMs = 0
        nowPlayingSent = false
        submitted = false
    }

    fun advance(
        item: QueueItem?,
        isPlaying: Boolean,
        positionMs: Long,
    ): Scrobble? {
        val previousPosition = lastPositionMs
        if (item?.id != songId || positionMs < previousPosition) {
            songId = item?.id
            durationMs = item?.durationMs ?: 0L
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
    }
}

class Scrobbler(
    private val sink: ServerActionSink,
    private val enabled: Flow<Boolean>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val policy: ScrobblePolicy = ScrobblePolicy(),
) {
    fun attach(
        state: StateFlow<PlaybackState>,
        positionMs: StateFlow<Long>,
    ) {
        scope.launch {
            combine(state, positionMs, enabled) { current, position, on -> Triple(current, position, on) }
                .collect { (current, position, on) ->
                    if (!on) {
                        policy.reset()
                        return@collect
                    }
                    val event = policy.advance(current.item, current.isPlaying, position) ?: return@collect
                    submit(event)
                }
        }
    }

    fun close() = scope.cancel()

    private suspend fun submit(event: Scrobble) {
        try {
            when (event) {
                is Scrobble.NowPlaying -> sink.nowPlaying(event.songId)
                is Scrobble.Submission -> sink.scrobble(event.songId, event.time)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }
}
