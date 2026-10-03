package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PendingAction
import com.subtracks.data.model.PendingActionKind
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import com.subtracks.data.source.subsonic.SubsonicException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class PendingServerActionSink(
    private val delegate: ServerActionSink,
    private val db: SubtracksDatabase,
    private val activeSourceId: suspend () -> Long?,
    private val offline: StateFlow<Boolean>,
    scope: CoroutineScope,
) : ServerActionSink {
    private val flushLock = Mutex()

    init {
        scope.launch {
            // The first emission is the StateFlow's default, not necessarily the persisted value, and
            // replaying onto the network must not race a cold start with offline mode on.
            offline
                .drop(1)
                .collect { isOffline -> if (!isOffline) flush() }
        }
    }

    override suspend fun nowPlaying(songId: String) {
        if (offline.value) return
        try {
            delegate.nowPlaying(songId)
            flush()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }

    override suspend fun scrobble(
        songId: String,
        time: Long,
    ) {
        if (offline.value) {
            queue(PendingActionKind.Scrobble, songId, null, time)
            return
        }
        try {
            delegate.scrobble(songId, time)
            flush()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            queue(PendingActionKind.Scrobble, songId, null, time)
        }
    }

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        val kind = if (starred) PendingActionKind.Star else PendingActionKind.Unstar
        if (offline.value) {
            if (!queue(kind, id, type.name, 0)) throw IllegalStateException("No active server")
            return
        }
        try {
            delegate.setStar(type, id, starred)
            flush()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (rejected: SubsonicException) {
            // The server answered and refused; retrying the same request will not help.
            throw rejected
        } catch (failure: Exception) {
            if (!queue(kind, id, type.name, 0)) throw failure
        }
    }

    override suspend fun flush() {
        flushLock.withLock {
            if (offline.value) return
            val sourceId = activeSourceId() ?: return
            val dao = db.pendingActionDao()
            for (action in dao.pending(sourceId)) {
                try {
                    deliver(action)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: IOException) {
                    // Still unreachable: keep this and the remaining actions for the next attempt.
                    return
                } catch (_: Exception) {
                    // The server answered and refused; drop the action rather than block the queue.
                }
                dao.delete(action.id)
            }
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

    private suspend fun queue(
        kind: PendingActionKind,
        targetId: String,
        starType: String?,
        time: Long,
    ): Boolean {
        val sourceId = activeSourceId() ?: return false
        val dao = db.pendingActionDao()
        if (starType != null) dao.clearStar(sourceId, starType, targetId)
        dao.insert(PendingAction(sourceId = sourceId, kind = kind, targetId = targetId, starType = starType, time = time))
        return true
    }
}
