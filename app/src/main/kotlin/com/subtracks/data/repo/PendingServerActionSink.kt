package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PendingAction
import com.subtracks.data.model.PendingActionKind
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import com.subtracks.data.source.subsonic.SubsonicException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PendingServerActionSink(
    private val delegate: ServerActionSink,
    private val db: SubtracksDatabase,
    private val activeSourceId: suspend () -> Long?,
    private val offline: StateFlow<Boolean>,
    offlinePreferences: Flow<Boolean>,
    networkAvailable: Flow<Boolean>,
    scope: CoroutineScope,
) : ServerActionSink {
    private val lock = Mutex()

    init {
        scope.launch {
            combine(offlinePreferences, networkAvailable) { isOffline, online -> !isOffline && online }
                .collect { ready ->
                    // The persisted flag and connectivity are authoritative, unlike the process
                    // flag's default at cold start, so wait for that flag to agree before replaying.
                    if (ready) {
                        offline.first { !it }
                        flush()
                    }
                }
        }
    }

    override suspend fun nowPlaying(songId: String) {
        if (offline.value) return
        if (attempt { delegate.nowPlaying(songId) } == null) flush()
    }

    override suspend fun scrobble(
        songId: String,
        time: Long,
    ) {
        lock.withLock {
            if (offline.value) {
                enqueueLocked(PendingActionKind.Scrobble, songId, null, time)
                return
            }
            when (val failure = attempt { delegate.scrobble(songId, time) }) {
                null -> drainSafelyLocked()
                else -> if (isRetryable(failure)) enqueueLocked(PendingActionKind.Scrobble, songId, null, time)
            }
        }
    }

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        val kind = if (starred) PendingActionKind.Star else PendingActionKind.Unstar
        lock.withLock {
            if (offline.value) {
                if (!enqueueLocked(kind, id, type.name, 0)) throw IllegalStateException("No active server")
                return
            }
            when (val failure = attempt { delegate.setStar(type, id, starred) }) {
                null -> {
                    // The absolute state just sent wins; a queued star for the same item is older.
                    clearStarLocked(type.name, id)
                    drainSafelyLocked()
                }

                else -> {
                    if (!isRetryable(failure) || !enqueueLocked(kind, id, type.name, 0)) throw failure
                }
            }
        }
    }

    override suspend fun flush() = lock.withLock { drainSafelyLocked() }

    private suspend fun drainLocked() {
        if (offline.value) return
        val sourceId = activeSourceId() ?: return
        val dao = db.pendingActionDao()
        for (action in dao.pending(sourceId)) {
            try {
                deliver(action)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                // Still unreachable: keep this and the remaining actions for the next attempt.
                if (isRetryable(failure)) return
                // The server answered and refused; drop the action rather than block the queue.
            }
            dao.delete(action.id)
        }
    }

    private suspend fun drainSafelyLocked() {
        try {
            drainLocked()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }

    private suspend fun deliver(action: PendingAction) {
        when (action.kind) {
            PendingActionKind.Scrobble -> {
                delegate.scrobble(action.targetId, action.time)
            }

            PendingActionKind.Star, PendingActionKind.Unstar -> {
                val type = StarType.valueOf(action.starType ?: error("Star action without a type"))
                delegate.setStar(type, action.targetId, action.kind == PendingActionKind.Star)
            }
        }
    }

    private suspend fun enqueueLocked(
        kind: PendingActionKind,
        targetId: String,
        starType: String?,
        time: Long,
    ): Boolean {
        val sourceId = activeSourceId() ?: return false
        db.pendingActionDao().replace(
            PendingAction(sourceId = sourceId, kind = kind, targetId = targetId, starType = starType, time = time),
        )
        return true
    }

    private suspend fun clearStarLocked(
        starType: String,
        targetId: String,
    ) {
        val sourceId = activeSourceId() ?: return
        db.pendingActionDao().clearStar(sourceId, starType, targetId)
    }

    private suspend fun attempt(block: suspend () -> Unit): Exception? =
        try {
            block()
            null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            failure
        }

    // A SubsonicException with a negative code is a transport or protocol failure (HTTP non-2xx,
    // malformed body), not a refusal, so it is still worth retrying.
    private fun isRetryable(failure: Exception): Boolean = failure !is SubsonicException || failure.code < 0
}
