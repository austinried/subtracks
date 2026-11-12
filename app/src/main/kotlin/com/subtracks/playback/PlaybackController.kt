package com.subtracks.playback

import com.subtracks.UiMessage
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.AudioEncoding
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.QueueOrigin
import com.subtracks.data.prefs.SongTapBehavior
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueRepository.QueueMutationScope
import com.subtracks.data.repo.QueueReset
import com.subtracks.data.repo.QueueResetItem
import com.subtracks.data.repo.QueueSnapshot
import com.subtracks.data.repo.QueueWindowItem
import com.subtracks.data.repo.Shuffle
import com.subtracks.data.repo.SourceRepository
import com.subtracks.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.random.Random

data class QueueItem(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val coverArtId: String?,
    val durationMs: Long? = null,
    val replayGain: ReplayGain = ReplayGain(),
    val albumContext: Boolean = false,
    val artists: List<ArtistRef> = emptyList(),
    val resetItem: QueueResetItem? = null,
)

enum class RepeatMode { Off, All, One }

data class PlaybackState(
    val item: QueueItem? = null,
    val context: QueueOrigin? = null,
    val position: Long? = null,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.Off,
    val layout: Long = 0,
)

private sealed interface QueueUndo {
    val entries: List<QueueEntry>
    val cursor: Long
    val current: Long?
    val shuffleSeed: Long?
    val upNext: List<QueueEntry>
    val upNextAnchor: Long
    val queueVersion: Long
}

private data class RemovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val position: Long,
    val item: QueueItem?,
    override val shuffleSeed: Long?,
    override val upNext: List<QueueEntry>,
    override val upNextAnchor: Long,
    override val queueVersion: Long,
) : QueueUndo

private data class MovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val from: Long,
    val to: Long,
    override val shuffleSeed: Long?,
    override val upNext: List<QueueEntry>,
    override val upNextAnchor: Long,
    override val queueVersion: Long,
) : QueueUndo

private data class ReloadUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val shuffleSeed: Long?,
    override val upNext: List<QueueEntry>,
    override val upNextAnchor: Long,
    override val queueVersion: Long,
) : QueueUndo {
    override val current: Long? = null
}

private data class PendingPlay(
    val entry: QueueEntry,
    val position: Long,
    val behavior: SongTapBehavior?,
)

class PlaybackController(
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
    private val connection: PlayerConnection,
    private val downloads: DownloadRepository,
    private val preferences: UserPreferences,
    private val showMessage: (UiMessage) -> Unit = {},
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val playbackDispatcher = scope.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs

    fun currentAudioEncoding(): AudioEncoding? = player?.audioEncoding

    private var player: PlayerHandle? = null
    private var connecting = false
    private var pendingPlay: PendingPlay? = null
    private var pendingShuffle: QueueEntry? = null
    private var positionJob: Job? = null
    private var windowJob: Job? = null
    private var bufferingJob: Job? = null
    private var stallJob: Job? = null
    private var showBuffering = false
    private var stalled = false

    private var snapshot: QueueSnapshot? = null
    private var queueSourceId: Long? = null
    private var windowStart = 0L
    private var windowEnd = -1L
    private var lastPosition: Long? = null
    private var layoutVersion = 0L
    private var updating = false
    private var lastEdit: QueueUndo? = null
    private var shuffleEnabled = false
    private var repeatMode = RepeatMode.Off
    private var endedHandled = false
    private var lastSavedPositionMs = 0L
    private val offline: Boolean get() = sourceRepository.offline.value
    private var windowPositions = LongArray(0)
    private var windowFiltered = false
    private var detachedWindow = false
    private var detachedPosition: Long? = null
    private var detachedFallbackPosition: Long? = null

    init {
        queueRepository.setResetListener { reset ->
            withContext(playbackDispatcher) {
                reconcileLibraryResetLocked(reset)
            }
        }
        scope.launch {
            sourceRepository.activeSourceId().collect { sourceId ->
                if (queueSourceId != null && sourceId != queueSourceId) stop()
            }
        }
        scope.launch {
            sourceRepository.offline.collect { reloadForOffline() }
        }
        scope.launch {
            sourceRepository.quality.drop(1).collect {
                try {
                    reloadQuality()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
        }
    }

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    fun connect() {
        if (player != null || connecting) return
        connecting = true
        connection.connect(
            onConnected = { handle ->
                connecting = false
                player = handle
                handle.addListener(playerListener)
                scope.launch {
                    val pending = pendingPlay
                    pendingPlay = null
                    val shuffle = pendingShuffle
                    pendingShuffle = null
                    when {
                        pending != null -> startPending(pending)
                        shuffle != null -> startShuffled(shuffle)
                        else -> restore()
                    }
                    _ready.value = true
                }
            },
            onDisconnected = {
                player = null
                connecting = false
                scope.launch { stop() }
                _ready.value = true
            },
        )
    }

    fun playAlbum(
        sourceId: Long,
        albumId: String,
        startOrdinal: Long,
    ) = play(queueRepository.albumEntry(sourceId, albumId), startOrdinal)

    fun playSong(
        sourceId: Long,
        songId: String,
    ) = play(queueRepository.songEntry(sourceId, songId), 0)

    fun playPlaylist(
        sourceId: Long,
        playlistId: String,
        startPosition: Long,
    ) = play(queueRepository.playlistEntry(sourceId, playlistId), startPosition)

    fun playGenre(
        sourceId: Long,
        genre: String,
        startOrdinal: Long,
    ) = play(queueRepository.genreEntry(sourceId, genre), startOrdinal)

    fun playDownloaded(
        sourceId: Long,
        startOrdinal: Long,
    ) = play(queueRepository.downloadedEntry(sourceId), startOrdinal)

    fun playStarred(
        sourceId: Long,
        startOrdinal: Long,
    ) = play(queueRepository.starredEntry(sourceId), startOrdinal)

    fun playAlbumInOrder(
        sourceId: Long,
        albumId: String,
    ) = play(queueRepository.albumEntry(sourceId, albumId), 0, SongTapBehavior.InOrder)

    fun playPlaylistInOrder(
        sourceId: Long,
        playlistId: String,
    ) = play(queueRepository.playlistEntry(sourceId, playlistId), 0, SongTapBehavior.InOrder)

    fun shuffleAlbum(
        sourceId: Long,
        albumId: String,
    ) = shufflePlay(queueRepository.albumEntry(sourceId, albumId))

    fun shufflePlaylist(
        sourceId: Long,
        playlistId: String,
    ) = shufflePlay(queueRepository.playlistEntry(sourceId, playlistId))

    fun shuffleArtist(
        sourceId: Long,
        artistId: String,
    ) = shufflePlay(queueRepository.artistEntry(sourceId, artistId))

    fun shuffleGenre(
        sourceId: Long,
        genre: String,
    ) = shufflePlay(queueRepository.genreEntry(sourceId, genre))

    fun shuffleStarred(sourceId: Long) = shufflePlay(queueRepository.starredEntry(sourceId))

    fun shuffleDownloaded(sourceId: Long) = shufflePlay(queueRepository.downloadedEntry(sourceId))

    private fun shufflePlay(entry: QueueEntry) {
        if (player == null) {
            pendingShuffle = entry
            connect()
            return
        }
        scope.launch { startShuffled(entry) }
    }

    private suspend fun startShuffled(entry: QueueEntry) {
        downloads.awaitLoaded()
        queueRepository.withMutationLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            lastEdit = null
            endedHandled = false
            val previous = snapshot
            val previousEntry = previous?.entries?.firstOrNull()?.entry
            val same = previousEntry != null && previousEntry.kind == entry.kind && previousEntry.refId == entry.refId
            val avoid = if (same) currentQueuePosition()?.let { previous.flatContext(previous.anchorContextPlay(it)) } else null
            replace(listOf(entry))
            var snap = snapshot()
            if (snap.contextSize == 0L) {
                stopLocked()
                return@withMutationLock
            }
            val start = randomIndex(snap.contextSize, avoid)
            setShuffle(pinnedSeed(snap.contextSize, start))
            shuffleEnabled = true
            queueSourceId = entry.sourceId
            queueRepository.setCursor(0)
            snap = snapshot()
            this@PlaybackController.snapshot = snap
            loadWindow(0, autoplay = true)
        }
    }

    private fun randomIndex(
        size: Long,
        avoid: Long?,
    ): Long {
        if (size <= 1L) return 0L
        val index = Random.nextLong(size)
        return if (avoid != null && index == avoid) (index + 1L) % size else index
    }

    // A seed whose permutation puts `flat` at play position 0, so the shuffled queue starts on the
    // track that should play instead of somewhere in the middle. Each random seed hits with
    // probability 1/size; fall back to a random seed for queues too large to search.
    private fun pinnedSeed(
        size: Long,
        flat: Long,
    ): Long {
        if (size <= 1L) return Random.nextLong()
        repeat(SEED_PIN_TRIES) {
            val seed = Random.nextLong()
            if (Shuffle.toFlat(seed, size, 0L) == flat) return seed
        }
        return Random.nextLong()
    }

    fun playAt(
        position: Long,
        resetVersion: Long? = null,
    ) {
        val requestedVersion = resetVersion ?: snapshot?.resetVersion ?: return
        scope.launch {
            downloads.awaitLoaded()
            queueRepository.withMutationLock {
                val player = player ?: return@withMutationLock
                var snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
                if (snapshot.resetVersion != requestedVersion) return@withMutationLock
                if (snapshot.size == 0L) return@withMutationLock
                val anchor = reanchorFor(snapshot, position)
                if (anchor.stale) return@withMutationLock
                if (anchor.changed) {
                    snapshot = readSnapshot()
                    this@PlaybackController.snapshot = snapshot
                    layoutVersion++
                }
                val requested = anchor.position.coerceIn(0, snapshot.size - 1)
                val target =
                    if (offline) {
                        downloadedAtOrAfter(snapshot, requested) ?: return@withMutationLock
                    } else {
                        requested
                    }
                windowJob?.cancel()
                queueRepository.setCursor(target)
                if (anchor.changed || !inWindow(target)) {
                    loadWindow(target, autoplay = true)
                } else {
                    player.ensurePrepared()
                    lastPosition = target
                    player.seekToIndex(windowIndexOf(target))
                    player.play()
                    shiftWindowLocked(target)
                    refresh(target)
                }
            }
        }
    }

    suspend fun removeAt(
        position: Long,
        resetVersion: Long? = null,
    ) {
        val requestedVersion = resetVersion ?: snapshot?.resetVersion ?: return
        queueRepository.withMutationLock {
            val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
            if (snapshot.resetVersion != requestedVersion) return@withMutationLock
            if (position !in 0 until snapshot.size) return@withMutationLock
            val player = player ?: return@withMutationLock
            val cursor = queueRepository.cursor()
            val entries = snapshot.entries.map { it.entry }
            val upNext = snapshot.upNext.map { it.entry }
            if (snapshot.shuffled && !snapshot.isUpNext(position)) return@withMutationLock
            if (snapshot.size == 1L) {
                if (!removeAt(snapshot, position)) return@withMutationLock
                if (detachedWindow && currentPosition() == null) {
                    val updated = readSnapshot()
                    this@PlaybackController.snapshot = updated
                    windowJob?.cancel()
                    rebuildDetachedSurvivors(updated, 0)
                    queueRepository.setCursor(0)
                    lastEdit =
                        ReloadUndo(
                            entries,
                            cursor,
                            snapshot.shuffleSeed,
                            upNext,
                            snapshot.upNextAnchor,
                            updated.version,
                        )
                    refresh(null)
                    return@withMutationLock
                }
                stopLocked()
                lastEdit =
                    ReloadUndo(
                        entries,
                        cursor,
                        snapshot.shuffleSeed,
                        upNext,
                        snapshot.upNextAnchor,
                        queueRepository.contentVersion(),
                    )
                return@withMutationLock
            }
            val current = currentQueuePosition() ?: return@withMutationLock
            val playing = player.playWhenReady
            val wasUpNext = snapshot.isUpNext(current)
            val removed = if (position == current) null else queueItemAt(snapshot, position)
            if (!removeAt(snapshot, position)) return@withMutationLock
            var updated = readSnapshot()
            this@PlaybackController.snapshot = updated
            windowJob?.cancel()
            if (updated.size == 0L) {
                stopLocked()
                lastEdit = ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor, updated.version)
                return@withMutationLock
            }
            // Derive the current track's new position from the edit. Resolving its song id after
            // the edit can match an identical song elsewhere (a duplicate in the context or the
            // block), so use positions throughout. Removing the playing track resumes at the same
            // slot, which now holds whatever was next (the block, if one is queued).
            var target =
                when {
                    position == current -> {
                        current
                    }

                    wasUpNext -> {
                        val blockIndex = snapshot.upNextIndex(current)
                        val newIndex =
                            if (snapshot.isUpNext(position) && position < current) blockIndex - 1 else blockIndex
                        updated.anchorPlay + 1 + newIndex
                    }

                    else -> {
                        // The current is a context track. Map its flat index through the (possibly
                        // re-derived) order, decremented when a context track before it was removed.
                        val flat = snapshot.flatContext(snapshot.contextPlay(current)) ?: 0L
                        val removedFlat =
                            if (snapshot.isUpNext(position)) null else snapshot.flatContext(snapshot.contextPlay(position))
                        val movedFlat = if (removedFlat != null && removedFlat < flat) flat - 1 else flat
                        updated.combined(if (snapshot.shuffled) updated.playContext(movedFlat) ?: 0L else movedFlat)
                    }
                }.coerceIn(0, updated.size - 1)
            // Keep the up-next block next: after a removal the cursor may have landed on a context
            // track that no longer carries the block (its anchor moved), so re-anchor it there.
            if (updated.upNextSize > 0L && !updated.isUpNext(target)) {
                val reanchored = reanchorFor(updated, target)
                if (reanchored.stale) return@withMutationLock
                if (reanchored.changed) {
                    updated = readSnapshot()
                    this@PlaybackController.snapshot = updated
                    target = reanchored.position
                }
            }
            lastPosition = target
            lastEdit =
                if (removed == null) {
                    ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor, updated.version)
                } else {
                    RemovedUndo(
                        entries,
                        cursor,
                        target,
                        position,
                        removed,
                        snapshot.shuffleSeed,
                        upNext,
                        snapshot.upNextAnchor,
                        updated.version,
                    )
                }
            if (preserveDetachedWindow(updated, target)) return@withMutationLock
            if (offline) {
                queueRepository.setCursor(target)
                loadWindow(target, autoplay = playing, startPositionMs = 0)
                return@withMutationLock
            }
            if (snapshot.upNextSize > 0L || updated.upNextSize > 0L || snapshot.shuffled) {
                queueRepository.setCursor(target)
                if (position != current) {
                    rebuildWindow(queueRepository.window(updated, target, QUEUE_WINDOW_RADIUS), target)
                    refresh(target)
                } else {
                    loadWindow(target, autoplay = playing, startPositionMs = 0)
                }
                return@withMutationLock
            }
            when {
                position == current -> {
                    loadWindow(target, autoplay = playing, startPositionMs = 0)
                }

                position < windowStart -> {
                    windowStart--
                    windowEnd--
                    shiftWindowLocked(target)
                }

                position <= windowEnd -> {
                    updating = true
                    player.removeAt((position - windowStart).toInt())
                    updating = false
                    windowEnd--
                    shiftWindowLocked(target)
                }

                else -> {
                    shiftWindowLocked(target)
                }
            }
            queueRepository.setCursor(target)
            refresh(target)
        }
    }

    suspend fun move(
        from: Long,
        to: Long,
        resetVersion: Long? = null,
    ) {
        if (from == to) return
        val requestedVersion = resetVersion ?: snapshot?.resetVersion ?: return
        queueRepository.withMutationLock {
            val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
            if (snapshot.resetVersion != requestedVersion) return@withMutationLock
            if (from !in 0 until snapshot.size || to !in 0 until snapshot.size) return@withMutationLock
            val player = player ?: return@withMutationLock
            val current = currentQueuePosition() ?: return@withMutationLock
            val cursor = queueRepository.cursor()
            val entries = snapshot.entries.map { it.entry }
            val upNext = snapshot.upNext.map { it.entry }
            val wasUpNext = snapshot.isUpNext(current)
            if (!move(snapshot, from, to)) return@withMutationLock
            val updated = readSnapshot()
            this@PlaybackController.snapshot = updated
            // The current track still exists, so derive its new position from the edit instead of
            // resolving its song id, which is ambiguous when the same song appears more than once.
            // A context move can also shift the anchor (and with it an up-next block), so read the
            // position back from the updated snapshot rather than only shifting `current`.
            val target =
                if (wasUpNext) {
                    val blockIndex = snapshot.upNextIndex(current)
                    val newIndex =
                        if (snapshot.isUpNext(from) && snapshot.isUpNext(to)) {
                            movedCursor(blockIndex, snapshot.upNextIndex(from), snapshot.upNextIndex(to))
                        } else {
                            blockIndex
                        }
                    updated.anchorPlay + 1 + newIndex
                } else {
                    val play = snapshot.contextPlay(current)
                    val newPlay =
                        if (!snapshot.isUpNext(from) && !snapshot.isUpNext(to)) {
                            movedCursor(play, snapshot.contextPlay(from), snapshot.contextPlay(to))
                        } else {
                            play
                        }
                    updated.combined(newPlay)
                }.coerceIn(0, updated.size - 1)
            lastPosition = target
            windowJob?.cancel()
            val fromInWindow = from in windowStart..windowEnd
            val toInWindow = to in windowStart..windowEnd
            lastEdit =
                if (fromInWindow == toInWindow) {
                    MovedUndo(entries, cursor, target, from, to, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor, updated.version)
                } else {
                    ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor, updated.version)
                }
            if (preserveDetachedWindow(updated, target)) return@withMutationLock
            if (offline) {
                queueRepository.setCursor(target)
                loadWindow(target, autoplay = player.playWhenReady, startPositionMs = player.currentPositionMs)
                return@withMutationLock
            }
            when {
                fromInWindow && toInWindow && (snapshot.upNextSize == 0L || snapshot.isUpNext(from)) -> {
                    updating = true
                    player.move((from - windowStart).toInt(), (to - windowStart).toInt())
                    updating = false
                }

                !fromInWindow && !toInWindow && (snapshot.upNextSize == 0L || snapshot.isUpNext(from)) -> {
                    // The moved track never enters the window, so the playlist is unchanged; only
                    // the window's positions shift, and a move that spans it shifts them by one.
                    when {
                        from < windowStart && to > windowEnd -> {
                            windowStart--
                            windowEnd--
                        }

                        from > windowEnd && to < windowStart -> {
                            windowStart++
                            windowEnd++
                        }
                    }
                }

                else -> {
                    rebuildWindow(queueRepository.window(updated, target, QUEUE_WINDOW_RADIUS), target)
                }
            }
            shiftWindowLocked(target)
            queueRepository.setCursor(target)
            refresh(target)
        }
    }

    suspend fun undo() =
        queueRepository.withMutationLock {
            val undo = lastEdit ?: return@withMutationLock
            if (queueRepository.contentVersion() != undo.queueVersion) {
                lastEdit = null
                return@withMutationLock
            }
            val player = player ?: return@withMutationLock
            val playing = player.playWhenReady
            val positionMs = player.currentPositionMs
            replace(undo.entries, undo.shuffleSeed, undo.upNext, undo.upNextAnchor)
            shuffleEnabled = undo.shuffleSeed != null
            val restored = readSnapshot()
            this@PlaybackController.snapshot = restored
            lastEdit = null
            if (restored.size == 0L) {
                stopLocked()
                return@withMutationLock
            }
            val target = undo.cursor.coerceIn(0, restored.size - 1)
            lastPosition = target
            val restoredId = queueRepository.itemAt(restored, target)?.song?.id
            windowJob?.cancel()
            if (preserveDetachedWindow(restored, target)) return@withMutationLock
            if (offline) {
                queueRepository.setCursor(target)
                loadWindow(target, autoplay = playing, startPositionMs = 0)
                return@withMutationLock
            }
            val canMirror =
                undo.current != null &&
                    player.currentItem?.id != null &&
                    player.currentItem?.id == restoredId &&
                    currentQueuePosition() == undo.current
            val mirrored =
                when {
                    !canMirror -> {
                        false
                    }

                    undo is RemovedUndo && undo.item != null -> {
                        if (undo.position < windowStart) {
                            windowStart++
                            windowEnd++
                        } else if (undo.position <= windowEnd) {
                            updating = true
                            player.insertAt((undo.position - windowStart).toInt(), undo.item)
                            updating = false
                            windowEnd++
                        }
                        true
                    }

                    undo is MovedUndo && undo.from in windowStart..windowEnd && undo.to in windowStart..windowEnd -> {
                        updating = true
                        player.move((undo.to - windowStart).toInt(), (undo.from - windowStart).toInt())
                        updating = false
                        true
                    }

                    undo is MovedUndo && undo.from !in windowStart..windowEnd && undo.to !in windowStart..windowEnd -> {
                        when {
                            undo.from < windowStart && undo.to > windowEnd -> {
                                windowStart++
                                windowEnd++
                            }

                            undo.from > windowEnd && undo.to < windowStart -> {
                                windowStart--
                                windowEnd--
                            }
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            if (mirrored) {
                shiftWindowLocked(target)
            } else {
                loadWindow(target, autoplay = playing, startPositionMs = if (canMirror) positionMs else 0)
            }
            queueRepository.setCursor(target)
            refresh(target)
        }

    fun togglePlayPause() {
        val player = player ?: return
        if (player.isEnded) {
            player.seekToIndex(player.currentIndex)
            player.play()
            return
        }
        player.ensurePrepared()
        if (player.playWhenReady) {
            player.pause()
            savePosition()
        } else {
            player.play()
        }
    }

    fun next() {
        scope.launch {
            downloads.awaitLoaded()
            queueRepository.withMutationLock {
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withMutationLock
                if (detachedWindow) {
                    val target =
                        detachedPosition
                            ?: if (offline && repeatMode != RepeatMode.Off) downloadedAtOrAfter(snapshot, 0) else null
                    target?.let { jumpToLocked(it, null) }
                    return@withMutationLock
                }
                val current = currentQueuePosition() ?: return@withMutationLock
                if (snapshot.isUpNext(current)) {
                    consumeUpNextLocked(current)
                    return@withMutationLock
                }
                val target =
                    if (offline) {
                        downloadedAtOrAfter(snapshot, current + 1)
                            ?: if (repeatMode != RepeatMode.Off) downloadedAtOrAfter(snapshot, 0L) else null
                    } else {
                        nextPosition(snapshot, current)
                    }
                target?.let { jumpToLocked(it, null) }
            }
        }
    }

    private fun nextPosition(
        snapshot: QueueSnapshot,
        current: Long,
    ): Long? =
        when {
            current < snapshot.size - 1 -> current + 1
            repeatMode != RepeatMode.Off -> 0L
            else -> null
        }

    private fun previousPosition(
        snapshot: QueueSnapshot,
        current: Long,
    ): Long? =
        when {
            current > 0 -> current - 1
            repeatMode == RepeatMode.All -> snapshot.size - 1
            else -> null
        }

    private suspend fun QueueMutationScope.consumeUpNextLocked(position: Long) {
        val snapshot = this@PlaybackController.snapshot ?: return
        if (!snapshot.isUpNext(position)) return
        if (position >= snapshot.size - 1 && repeatMode == RepeatMode.Off) return
        if (!removeUpNextAt(snapshot, snapshot.upNextIndex(position))) return
        val updated = readSnapshot()
        this@PlaybackController.snapshot = updated
        layoutVersion++
        windowJob?.cancel()
        endedHandled = false
        val next = if (position < updated.size) position else 0L
        queueRepository.setCursor(next)
        if (offline) {
            loadWindow(next, autoplay = true)
            return
        }
        val player = player
        if (next == position && player != null && position - windowStart in 0 until player.itemCount) {
            updating = true
            player.removeAt((position - windowStart).toInt())
            updating = false
            windowEnd--
            shiftWindowLocked(next)
            refresh(next)
        } else {
            loadWindow(next, autoplay = true)
        }
    }

    fun clearUpNext() {
        scope.launch {
            queueRepository.withMutationLock {
                val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
                if (snapshot.upNextSize == 0L) return@withMutationLock
                player ?: return@withMutationLock
                val current = currentQueuePosition() ?: return@withMutationLock
                val wasUpNext = snapshot.isUpNext(current)
                clearUpNext()
                val updated = readSnapshot()
                this@PlaybackController.snapshot = updated
                layoutVersion++
                windowJob?.cancel()
                endedHandled = false
                val contextPlay = if (wasUpNext) snapshot.anchorPlay + 1 else snapshot.contextPlay(current)
                val target = contextPlay.coerceIn(0, (updated.size - 1).coerceAtLeast(0))
                queueRepository.setCursor(target)
                lastPosition = target
                if (preserveDetachedWindow(updated, target)) return@withMutationLock
                if (wasUpNext) {
                    loadWindow(target, autoplay = true)
                } else {
                    rebuildWindow(queueRepository.window(updated, target, QUEUE_WINDOW_RADIUS), target)
                    refresh(target)
                }
            }
        }
    }

    fun previous() {
        scope.launch {
            downloads.awaitLoaded()
            queueRepository.withMutationLock {
                val player = player ?: return@withMutationLock
                if (player.currentPositionMs > RESTART_THRESHOLD_MS) {
                    player.seekTo(0)
                    refresh()
                    return@withMutationLock
                }
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withMutationLock
                val current = currentQueuePosition() ?: return@withMutationLock
                val target =
                    if (offline) {
                        downloadedAtOrBefore(snapshot, current - 1)
                            ?: if (repeatMode == RepeatMode.All) downloadedAtOrBefore(snapshot, snapshot.size - 1) else null
                    } else {
                        previousPosition(snapshot, current)
                    }
                if (target != null) {
                    jumpToLocked(target, null)
                } else {
                    player.seekTo(0)
                    refresh()
                }
            }
        }
    }

    fun previousTrack() {
        scope.launch {
            downloads.awaitLoaded()
            queueRepository.withMutationLock {
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withMutationLock
                val current = currentQueuePosition() ?: return@withMutationLock
                val target =
                    if (offline) {
                        downloadedAtOrBefore(snapshot, current - 1)
                            ?: if (repeatMode == RepeatMode.All) downloadedAtOrBefore(snapshot, snapshot.size - 1) else null
                    } else {
                        previousPosition(snapshot, current)
                    }
                target?.let { jumpToLocked(it, null) }
            }
        }
    }

    fun addToQueue(
        sourceId: Long,
        kind: QueueKind,
        refId: String,
    ) = enqueue(sourceId, kind, refId, playNext = false)

    fun playNext(
        sourceId: Long,
        kind: QueueKind,
        refId: String,
    ) = enqueue(sourceId, kind, refId, playNext = true)

    private fun enqueue(
        sourceId: Long,
        kind: QueueKind,
        refId: String,
        playNext: Boolean,
    ) {
        val entry = QueueEntry(position = 0, sourceId = sourceId, kind = kind, refId = refId)
        scope.launch { enqueue(entry, playNext) }
    }

    private suspend fun enqueue(
        entry: QueueEntry,
        playNext: Boolean,
    ) = queueRepository.withMutationLock {
        val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
        val player = player ?: return@withMutationLock
        if (snapshot.size == 0L) return@withMutationLock
        val current = currentQueuePosition() ?: return@withMutationLock
        val position = addUpNext(current, entry, playNext)
        val updated = readSnapshot()
        this@PlaybackController.snapshot = updated
        layoutVersion++
        queueRepository.setCursor(position)
        windowJob?.cancel()
        rebuildWindow(queueRepository.window(updated, position, QUEUE_WINDOW_RADIUS), position)
        refresh(position)
    }

    fun cycleRepeat() {
        val next =
            when (repeatMode) {
                RepeatMode.Off -> RepeatMode.All
                RepeatMode.All -> RepeatMode.One
                RepeatMode.One -> RepeatMode.Off
            }
        repeatMode = next
        player?.setRepeatOne(next == RepeatMode.One)
        scope.launch { queueRepository.setRepeat(next.ordinal) }
        refresh()
    }

    fun toggleShuffle() {
        scope.launch {
            queueRepository.withMutationLock {
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withMutationLock
                player ?: return@withMutationLock
                val current = currentQueuePosition() ?: return@withMutationLock
                val wasUpNext = snapshot.isUpNext(current)
                val blockIndex = snapshot.upNextIndex(current)
                val flat = snapshot.flatContext(snapshot.anchorContextPlay(current)) ?: return@withMutationLock
                windowJob?.cancel()
                val enabling = !shuffleEnabled
                if (enabling) {
                    setShuffle(pinnedSeed(snapshot.contextSize, flat))
                } else {
                    setShuffle(null)
                }
                shuffleEnabled = enabling
                val reordered = readSnapshot()
                this@PlaybackController.snapshot = reordered
                val position =
                    when {
                        wasUpNext -> reordered.anchorPlay + 1 + blockIndex
                        enabling -> reordered.combined(0L)
                        else -> reordered.combined(flat)
                    }
                val anchor = reanchorFor(reordered, position)
                if (anchor.stale) return@withMutationLock
                val finalSnapshot = if (anchor.changed) readSnapshot() else reordered
                if (anchor.changed) layoutVersion++
                this@PlaybackController.snapshot = finalSnapshot
                queueRepository.setCursor(anchor.position)
                rebuildWindow(queueRepository.window(finalSnapshot, anchor.position, QUEUE_WINDOW_RADIUS), anchor.position)
                endedHandled = false
                refresh(anchor.position)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        persistPosition(positionMs)
    }

    fun coverArt(
        item: QueueItem?,
        thumbnail: Boolean = false,
    ): CoverArtRef? = sourceRepository.coverArt(item?.coverArtId, thumbnail)

    suspend fun upcomingItem(): QueueItem? {
        var current: QueueSnapshot? = null
        var position: Long? = null
        var isOffline = false
        queueRepository.withMutationLock {
            current = snapshot
            position = currentQueuePosition()
            isOffline = offline
        }
        val snapshot = current ?: return null
        val detachedNext = detachedWindow && currentPosition() == null
        if (!isOffline) {
            val at = position ?: return null
            if (detachedNext) return queueItemAt(snapshot, at)
            return nextPosition(snapshot, at)
                ?.takeIf { it != at }
                ?.let { queueItemAt(snapshot, it) }
        }
        downloads.awaitLoaded()
        val at = position
        val target =
            if (at == null) {
                if (detachedNext && repeatMode != RepeatMode.Off) downloadedAtOrAfter(snapshot, 0L) else null
            } else {
                val from = if (detachedNext) at else at + 1
                downloadedAtOrAfter(snapshot, from)
                    ?: if (repeatMode != RepeatMode.Off) downloadedAtOrAfter(snapshot, 0L) else null
            }
        return target?.takeIf { it != at || detachedNext }?.let { queueItemAt(snapshot, it) }
    }

    suspend fun previousItem(): QueueItem? {
        var current: QueueSnapshot? = null
        var position: Long? = null
        var isOffline = false
        queueRepository.withMutationLock {
            current = snapshot
            position = currentQueuePosition()
            isOffline = offline
        }
        val snapshot = current ?: return null
        val at = position ?: return null
        if (!isOffline) {
            return previousPosition(snapshot, at)
                ?.takeIf { it != at }
                ?.let { queueItemAt(snapshot, it) }
        }
        downloads.awaitLoaded()
        val target =
            downloadedAtOrBefore(snapshot, at - 1)
                ?: if (repeatMode == RepeatMode.All) downloadedAtOrBefore(snapshot, snapshot.size - 1) else null
        return target?.takeIf { it != at }?.let { queueItemAt(snapshot, it) }
    }

    private fun play(
        entry: QueueEntry,
        startPosition: Long,
        behavior: SongTapBehavior? = null,
    ) {
        if (player == null) {
            pendingPlay = PendingPlay(entry, startPosition, behavior)
            connect()
            return
        }
        scope.launch {
            startLock.withLock {
                startLocked(entry, startPosition, behavior)
            }
        }
    }

    private suspend fun startPending(pending: PendingPlay) {
        startLock.withLock {
            startLocked(pending.entry, pending.position, pending.behavior)
        }
    }

    private suspend fun startLocked(
        entry: QueueEntry,
        startPosition: Long,
        behavior: SongTapBehavior?,
    ) {
        downloads.awaitLoaded()
        queueRepository.withMutationLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            lastEdit = null
            endedHandled = false
            val resolved = behavior ?: preferences.songTapBehavior().first()
            val modes = queueRepository.modes()
            shuffleEnabled =
                when (resolved) {
                    SongTapBehavior.InOrder -> false
                    SongTapBehavior.KeepShuffle -> modes.shuffle
                    SongTapBehavior.Shuffle -> true
                }
            repeatMode = modes.repeat.toRepeatMode()
            player?.setRepeatOne(repeatMode == RepeatMode.One)
            replace(listOf(entry))
            val snapshot = snapshot()
            this@PlaybackController.snapshot = snapshot
            queueSourceId = entry.sourceId
            if (snapshot.size == 0L) {
                stopLocked()
                return@withMutationLock
            }
            val start = startPosition.coerceIn(0, snapshot.contextSize - 1)
            val position =
                if (shuffleEnabled) {
                    setShuffle(pinnedSeed(snapshot.contextSize, start))
                    0L
                } else {
                    start
                }
            queueRepository.setCursor(position)
            this@PlaybackController.snapshot = readSnapshot()
            loadWindow(position, autoplay = true)
        }
    }

    private suspend fun restore() {
        downloads.awaitLoaded()
        queueRepository.withMutationLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            endedHandled = false
            val modes = queueRepository.modes()
            shuffleEnabled = modes.shuffle
            repeatMode = modes.repeat.toRepeatMode()
            player?.setRepeatOne(repeatMode == RepeatMode.One)
            var snapshot = snapshot()
            if (shuffleEnabled && !snapshot.shuffled && snapshot.contextSize > 0L) {
                val start = snapshot.anchorContextPlay(queueRepository.cursor()).coerceIn(0, snapshot.contextSize - 1)
                val flat = snapshot.flatContext(start) ?: start
                setShuffle(pinnedSeed(snapshot.contextSize, flat))
                queueRepository.setCursor(0)
                snapshot = snapshot()
            } else if (!snapshot.shuffled) {
                shuffleEnabled = false
            }
            this@PlaybackController.snapshot = snapshot
            val queueSourceId =
                snapshot.entries
                    .firstOrNull()
                    ?.entry
                    ?.sourceId
            val activeId = sourceRepository.activeSourceIdOnce()
            if (snapshot.size == 0L || (queueSourceId != null && activeId != null && queueSourceId != activeId)) {
                stopLocked()
                return@withMutationLock
            }
            this@PlaybackController.queueSourceId = queueSourceId
            val savedPositionMs = queueRepository.cursorPositionMs().coerceAtLeast(0)
            val cursor = queueRepository.cursor().coerceIn(0, snapshot.size - 1)
            val anchor = reanchorFor(snapshot, cursor)
            if (anchor.stale) return@withMutationLock
            if (anchor.changed) {
                snapshot = snapshot()
                this@PlaybackController.snapshot = snapshot
                queueRepository.setCursor(anchor.position)
                queueRepository.setPosition(savedPositionMs)
            }
            lastSavedPositionMs = savedPositionMs
            loadWindow(anchor.position, autoplay = false, startPositionMs = savedPositionMs)
        }
    }

    private suspend fun stop() = queueRepository.withMutationLock { stopLocked() }

    private fun stopLocked() {
        windowJob?.cancel()
        bufferingJob?.cancel()
        bufferingJob = null
        stallJob?.cancel()
        stallJob = null
        stalled = false
        showBuffering = false
        snapshot = null
        queueSourceId = null
        windowStart = 0
        windowEnd = -1
        windowPositions = LongArray(0)
        windowFiltered = false
        lastPosition = null
        lastEdit = null
        player?.run {
            stop()
            clear()
        }
        _state.value = PlaybackState()
        _positionMs.value = 0L
        stopPositionTicker()
    }

    private suspend fun loadWindow(
        position: Long,
        autoplay: Boolean,
        startPositionMs: Long = 0,
    ) {
        val player = player ?: return
        val snapshot = snapshot ?: return
        val requested =
            if (offline) {
                downloadedAtOrAfter(snapshot, position) ?: downloadedAtOrBefore(snapshot, position - 1) ?: run {
                    stopLocked()
                    return
                }
            } else {
                position
            }
        val raw = queueRepository.window(snapshot, requested, QUEUE_WINDOW_RADIUS)
        val window = if (offline) raw.filter { downloads.localUri(it.item.song.id) != null } else raw
        if (window.isEmpty()) {
            stopLocked()
            return
        }
        var startIndex = window.indexOfFirst { it.position == requested }
        if (startIndex < 0) {
            startIndex = window.indexOfFirst { it.position > requested }
            if (startIndex < 0) startIndex = window.lastIndex
        }
        val actual = window[startIndex].position
        clearDetached()
        windowPositions = LongArray(window.size) { window[it].position }
        windowFiltered = offline
        windowStart = window.first().position
        windowEnd = window.last().position
        lastPosition = actual
        if (actual != requested) queueRepository.setCursor(actual)
        updating = true
        player.setWindow(window.map { it.playerItem(snapshot) }, startIndex, startPositionMs)
        updating = false
        if (autoplay) {
            player.prepare()
            player.play()
        }
        refresh(actual)
    }

    private suspend fun reloadQuality() =
        queueRepository.withMutationLock {
            val sourceId = queueSourceId
            if (sourceId == null || sourceRepository.activeSourceIdOnce() != sourceId) return@withMutationLock
            reloadWindowLocked()
        }

    fun refreshMediaItems() {
        scope.launch {
            downloads.awaitLoaded()
            queueRepository.withMutationLock {
                val player = player ?: return@withMutationLock
                val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
                if (detachedWindow && currentPosition() == null) return@withMutationLock
                if (snapshot.size == 0L || player.isEnded) return@withMutationLock
                if (offline) {
                    reloadWindowLocked()
                    return@withMutationLock
                }
                val position = (currentQueuePosition() ?: return@withMutationLock).coerceIn(0, snapshot.size - 1)
                val window = queueRepository.window(snapshot, position, QUEUE_WINDOW_RADIUS)
                val uris = player.itemUris()
                if (window.size != player.itemCount || uris.size != window.size) {
                    reloadWindowLocked()
                    return@withMutationLock
                }
                // Only an item whose local file appeared or went away needs rebuilding. Re-setting
                // the whole window would re-prepare the player and audibly pause playback.
                var replaced = false
                window.forEachIndexed { index, entry ->
                    val local = downloads.localUri(entry.item.song.id) != null
                    if (local != (uris[index]?.startsWith(FILE_SCHEME) == true)) {
                        player.replaceItem(index, entry.playerItem(snapshot))
                        replaced = true
                    }
                }
                if (replaced) refresh(position)
            }
        }
    }

    private suspend fun reloadWindowLocked() {
        val player = player ?: return
        val snapshot = snapshot ?: return
        if (detachedWindow && currentPosition() == null) return
        if (snapshot.size == 0L) return
        if (player.isEnded) return
        val position = (currentQueuePosition() ?: return).coerceIn(0, snapshot.size - 1)
        val positionMs = player.currentPositionMs
        loadWindow(position, autoplay = player.playWhenReady, startPositionMs = positionMs)
    }

    private suspend fun QueueMutationScope.jumpToLocked(
        target: Long,
        requestedVersion: Long?,
    ) {
        val player = player ?: return
        if (requestedVersion != null && snapshot?.resetVersion != requestedVersion) return
        var snapshot = readSnapshot()
        if (snapshot.size == 0L) return
        val anchor = reanchorFor(snapshot, target)
        if (anchor.stale) return
        if (anchor.changed) {
            snapshot = readSnapshot()
            layoutVersion++
        }
        this@PlaybackController.snapshot = snapshot
        val current = currentPosition() ?: Long.MIN_VALUE
        val requested = anchor.position.coerceIn(0, snapshot.size - 1)
        val dest =
            if (offline) {
                downloadedAtOrAfter(snapshot, requested) ?: return
            } else {
                requested
            }
        val wasEnded = player.isEnded
        if (!anchor.changed && dest == current && !wasEnded) return
        windowJob?.cancel()
        queueRepository.setCursor(dest)
        if (anchor.changed || !inWindow(dest)) {
            loadWindow(dest, autoplay = player.playWhenReady || wasEnded)
            endedHandled = false
            return
        }
        lastPosition = dest
        shiftWindowLocked(dest)
        player.ensurePrepared()
        player.seekToIndex(windowIndexOf(dest))
        if (wasEnded) player.play()
        endedHandled = false
        refresh(dest)
    }

    private suspend fun onPositionChanged() {
        queueRepository.withMutationLock {
            val player = player ?: return@withMutationLock
            pruneDetached(player)
            val snapshot = snapshot ?: return@withMutationLock
            if (snapshot.size == 0L) return@withMutationLock
            val position = (currentQueuePosition() ?: return@withMutationLock).coerceIn(0, snapshot.size - 1)
            val previous = lastPosition
            lastPosition = position
            if (previous != null && previous != position && snapshot.isUpNext(previous)) {
                consumeUpNextLocked(previous)
                return@withMutationLock
            }
            lastSavedPositionMs = 0
            queueRepository.setCursor(position)
            refresh(position)
            scheduleWindowShift(position)
        }
    }

    private fun scheduleWindowShift(center: Long) {
        windowJob?.cancel()
        windowJob =
            scope.launch {
                delay(WINDOW_SHIFT_DELAY_MS)
                shiftWindow(center)
                refresh(center)
            }
    }

    private suspend fun shiftWindow(center: Long) = queueRepository.withMutationLock { shiftWindowLocked(center) }

    private suspend fun QueueMutationScope.shiftWindowLocked(center: Long) {
        val player = player ?: return
        if (offline || detachedWindow) {
            lastPosition = center
            return
        }
        val previous = snapshot
        val snapshot = readSnapshot()
        this@PlaybackController.snapshot = snapshot
        if (snapshot.size == 0L) return
        if (previous != null && previous.shuffled != snapshot.shuffled) {
            val currentId = player.currentItem?.id
            val position =
                (currentId?.let { queueRepository.combinedIndexOf(snapshot, it) } ?: center)
                    .coerceIn(0, snapshot.size - 1)
            queueRepository.setCursor(position)
            rebuildWindow(queueRepository.window(snapshot, position, QUEUE_WINDOW_RADIUS), position)
            refresh(position)
            return
        }
        val target = center.coerceIn(0, snapshot.size - 1)
        val desiredStart = (target - QUEUE_WINDOW_RADIUS).coerceAtLeast(0)
        val desiredEnd = (target + QUEUE_WINDOW_RADIUS).coerceAtMost(snapshot.size - 1)
        updating = true
        try {
            while (windowStart > desiredStart) {
                val item = queueItemAt(snapshot, windowStart - 1) ?: break
                player.addFirst(item)
                windowStart--
            }
            while (windowStart < desiredStart) {
                player.removeFirst()
                windowStart++
            }
            while (windowEnd < desiredEnd) {
                val item = queueItemAt(snapshot, windowEnd + 1) ?: break
                player.addLast(item)
                windowEnd++
            }
            while (windowEnd > desiredEnd) {
                player.removeLast()
                windowEnd--
            }
        } finally {
            updating = false
        }
        lastPosition = target
    }

    private suspend fun QueueMutationScope.reconcileLibraryResetLocked(reset: QueueReset) {
        val player = player ?: return
        val previous = snapshot ?: return
        if (previous.version > reset.snapshot.version) return
        var current = reset.point.current
        val actualItem = player.currentItem
        val actualId = actualItem?.id
        val actualPosition = currentPosition()
        if (actualId != null && actualPosition != null) {
            val upNext = previous.isUpNext(actualPosition)
            val resolved = actualItem.resetItem?.takeIf { it.songId == actualId && it.upNext == upNext }
            val captured =
                current?.takeIf {
                    it.songId == actualId && it.position == actualPosition && it.upNext == upNext &&
                        resolved != null && it.origin == resolved.origin &&
                        it.entryOccurrence == resolved.entryOccurrence && it.occurrence == resolved.occurrence
                }
            current =
                captured
                    ?: resolved?.copy(position = actualPosition)
                    ?: if (previous.locate(actualPosition)?.first?.origin != null) {
                        queueRepository.resetItemAt(previous, actualPosition, actualId)
                    } else {
                        current?.copy(songId = actualId, position = actualPosition, upNext = upNext)
                    }
        }
        lastEdit = null
        windowJob?.cancel()
        endedHandled = false
        var updated = reset.snapshot
        shuffleEnabled = updated.shuffled
        layoutVersion++
        this@PlaybackController.snapshot = updated
        val target = current?.let { queueRepository.remapPosition(updated, it) }
        if (target != null) {
            val anchor = reanchorFor(updated, target)
            if (anchor.stale) return
            if (anchor.changed) {
                updated = snapshot()
                this@PlaybackController.snapshot = updated
            }
            val position = anchor.position.coerceIn(0, updated.size - 1)
            queueRepository.setCursor(position)
            if (detachedWindow && currentPosition() == null) {
                rebuildDetachedSurvivors(updated, position)
            } else {
                clearDetached()
                rebuildWindow(queueRepository.window(updated, position, QUEUE_WINDOW_RADIUS), position)
            }
            refresh(position)
            return
        }

        if (player.itemCount == 0) {
            if (updated.size == 0L) {
                stopLocked()
            } else {
                loadWindow(0, autoplay = false)
            }
            return
        }
        val nearest = (current?.position ?: lastPosition ?: 0L).coerceIn(0, (updated.size - 1).coerceAtLeast(0))
        rebuildDetachedSurvivors(updated, nearest)
        queueRepository.setCursor(lastPosition ?: 0L)
        refresh(lastPosition)
    }

    private fun trimToCurrent(player: PlayerHandle) {
        updating = true
        try {
            while (player.itemCount > player.currentIndex + 1) player.removeLast()
            repeat(player.currentIndex) { player.removeFirst() }
        } finally {
            updating = false
        }
    }

    private fun markDetached(
        window: List<QueueWindowItem>,
        position: Long?,
        fallbackPosition: Long?,
    ) {
        detachedWindow = true
        detachedPosition = position
        detachedFallbackPosition = fallbackPosition
        windowFiltered = offline
        windowPositions = LongArray(window.size + 1) { if (it == 0) DETACHED_POSITION else window[it - 1].position }
        windowStart = window.firstOrNull()?.position ?: 0L
        windowEnd = window.lastOrNull()?.position ?: -1L
    }

    private fun clearDetached() {
        detachedWindow = false
        detachedPosition = null
        detachedFallbackPosition = null
        windowFiltered = offline
    }

    private fun pruneDetached(player: PlayerHandle) {
        if (!detachedWindow) return
        val index = player.currentIndex
        if (index <= 0) return
        updating = true
        try {
            repeat(index) { player.removeFirst() }
        } finally {
            updating = false
        }
        windowPositions = windowPositions.copyOfRange(index, windowPositions.size)
        clearDetached()
        windowStart = windowPositions.firstOrNull() ?: 0L
        windowEnd = windowPositions.lastOrNull() ?: -1L
    }

    private fun currentQueuePosition(): Long? = currentPosition() ?: detachedPosition ?: detachedFallbackPosition

    private suspend fun rebuildWindow(
        window: List<QueueWindowItem>,
        position: Long,
    ) {
        val player = player ?: return
        val snapshot = this.snapshot ?: return
        if (detachedWindow && currentPosition() == null) {
            rebuildDetachedSurvivors(snapshot, position)
            return
        }
        val ordered =
            if (offline) window.filter { downloads.localUri(it.item.song.id) != null } else window
        val index = ordered.indexOfFirst { it.position == position }
        if (index < 0) {
            loadWindow(position, autoplay = player.playWhenReady, startPositionMs = player.currentPositionMs)
            return
        }
        // A track downloaded while it was streaming still holds a stream URL; fix the current item's
        // source in place (without resetting the window) so an offline switch can play the local file.
        val current = ordered[index]
        val wantsLocal = downloads.localUri(current.item.song.id) != null
        val isLocal = player.itemUris().getOrNull(player.currentIndex)?.startsWith(FILE_SCHEME) == true
        if (wantsLocal != isLocal) player.replaceItem(player.currentIndex, current.playerItem(snapshot))
        clearDetached()
        windowPositions = LongArray(ordered.size) { ordered[it].position }
        windowFiltered = offline
        windowStart = ordered.first().position
        windowEnd = ordered.last().position
        lastPosition = position
        updating = true
        try {
            repeat(player.currentIndex) { player.removeFirst() }
            while (player.itemCount > 1) player.removeLast()
            for (i in index - 1 downTo 0) player.addFirst(ordered[i].playerItem(snapshot))
            for (i in index + 1 until ordered.size) player.addLast(ordered[i].playerItem(snapshot))
            if (player.currentIndex != index) player.seekToIndex(index)
        } finally {
            updating = false
        }
    }

    private suspend fun preserveDetachedWindow(
        snapshot: QueueSnapshot,
        position: Long,
    ): Boolean {
        if (!detachedWindow || currentPosition() != null) return false
        queueRepository.setCursor(position)
        rebuildDetachedSurvivors(snapshot, position)
        refresh(lastPosition)
        return true
    }

    private suspend fun rebuildDetachedSurvivors(
        snapshot: QueueSnapshot,
        position: Long,
    ) {
        val player = player ?: return
        val nearest = position.coerceIn(0, (snapshot.size - 1).coerceAtLeast(0))
        val window =
            queueRepository
                .range(snapshot, nearest, (nearest + QUEUE_WINDOW_RADIUS).coerceAtMost(snapshot.size - 1))
                .filter { !offline || downloads.localUri(it.item.song.id) != null }
        trimToCurrent(player)
        updating = true
        try {
            window.forEachIndexed { index, entry -> player.insertAt(index + 1, entry.playerItem(snapshot)) }
        } finally {
            updating = false
        }
        val next = window.firstOrNull()?.position
        markDetached(window, next, nearest.takeIf { snapshot.size > 0L })
        lastPosition = next
    }

    private fun currentPosition(): Long? {
        val player = player ?: return null
        if (player.itemCount == 0) return null
        val index = player.currentIndex
        val position =
            if (windowFiltered ||
                detachedWindow
            ) {
                windowPositions.getOrNull(index) ?: (windowStart + index)
            } else {
                (windowStart + index)
            }
        return position.takeIf { it >= 0L }
    }

    private fun windowIndexOf(position: Long): Int =
        if (windowFiltered || detachedWindow) {
            windowPositions.indexOf(position)
        } else {
            (position - windowStart).toInt()
        }

    private fun inWindow(position: Long): Boolean =
        if (windowFiltered || detachedWindow) {
            windowPositions.contains(position)
        } else {
            position in windowStart..windowEnd
        }

    private suspend fun downloadedAtOrAfter(
        snapshot: QueueSnapshot,
        from: Long,
    ): Long? = downloadedInDirection(snapshot, from, 1L)

    private suspend fun downloadedAtOrBefore(
        snapshot: QueueSnapshot,
        from: Long,
    ): Long? = downloadedInDirection(snapshot, from, -1L)

    private suspend fun downloadedInDirection(
        snapshot: QueueSnapshot,
        from: Long,
        step: Long,
    ): Long? =
        withContext(Dispatchers.IO) {
            var position = from
            var scanned = 0L
            while (position in 0 until snapshot.size && scanned < OFFLINE_SCAN_LIMIT) {
                val songId = queueRepository.itemAt(snapshot, position)?.song?.id
                if (songId != null && downloads.localUri(songId) != null) return@withContext position
                position += step
                scanned++
            }
            null
        }

    private suspend fun reloadForOffline() {
        downloads.awaitLoaded()
        queueRepository.withMutationLock {
            val player = player ?: return@withMutationLock
            val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
            if (snapshot.size == 0L) return@withMutationLock
            val current = currentQueuePosition() ?: return@withMutationLock
            val detached = detachedWindow && currentPosition() == null
            windowJob?.cancel()
            val target =
                if (offline) {
                    downloadedAtOrAfter(snapshot, current) ?: if (detached) {
                        current
                    } else {
                        stopLocked()
                        return@withMutationLock
                    }
                } else {
                    current
                }
            queueRepository.setCursor(target)
            if (preserveDetachedWindow(snapshot, target)) return@withMutationLock
            if (target == current) {
                // Keep the playing item in place and only add/remove its neighbours, so a downloaded
                // track does not re-prepare (and audibly gap) when offline mode is toggled.
                rebuildWindow(queueRepository.window(snapshot, target, QUEUE_WINDOW_RADIUS), target)
                refresh(target)
            } else {
                loadWindow(target, autoplay = player.playWhenReady, startPositionMs = 0)
            }
        }
    }

    private fun movedCursor(
        current: Long,
        from: Long,
        to: Long,
    ): Long =
        when {
            from == current -> to
            from < current && to >= current -> current - 1
            from > current && to <= current -> current + 1
            else -> current
        }

    private val playerListener =
        object : PlayerHandle.Listener {
            override fun onTransition() {
                endedHandled = false
                if (updating) return
                scope.launch { onPositionChanged() }
            }

            override fun onEvents() {
                if (player?.isEnded == true) handleEnded() else endedHandled = false
                refresh()
            }

            override fun onError(message: UiMessage) {
                showMessage(message)
                refresh()
            }
        }

    private fun handleEnded() {
        if (endedHandled) return
        endedHandled = true
        val endedSnapshot = snapshot ?: return
        val requestedVersion = endedSnapshot.resetVersion
        val endedPosition = currentQueuePosition()
        if (endedSnapshot.size == 0L) {
            scope.launch {
                queueRepository.withMutationLock {
                    if (!endJobIsCurrent(endedSnapshot, endedPosition)) return@withMutationLock
                    stopLocked()
                }
            }
            return
        }
        if (offline) {
            scope.launch {
                downloads.awaitLoaded()
                queueRepository.withMutationLock {
                    if (!endJobIsCurrent(endedSnapshot, endedPosition)) return@withMutationLock
                    val snapshot = this@PlaybackController.snapshot ?: return@withMutationLock
                    if (detachedWindow && detachedPosition == null) {
                        val target = if (repeatMode == RepeatMode.All) downloadedAtOrAfter(snapshot, 0L) else null
                        if (target != null) {
                            jumpToLocked(target, requestedVersion)
                        } else {
                            stopLocked()
                        }
                        return@withMutationLock
                    }
                    val current = endedPosition ?: return@withMutationLock
                    val from = if (detachedWindow) current else current + 1
                    val target =
                        downloadedAtOrAfter(snapshot, from)
                            ?: if (repeatMode == RepeatMode.All) downloadedAtOrAfter(snapshot, 0L) else null
                    target?.let { jumpToLocked(it, requestedVersion) }
                }
            }
            return
        }
        if (repeatMode == RepeatMode.All) {
            scope.launch {
                queueRepository.withMutationLock {
                    if (!endJobIsCurrent(endedSnapshot, endedPosition)) return@withMutationLock
                    if (repeatMode != RepeatMode.All) return@withMutationLock
                    jumpToLocked(0, requestedVersion)
                }
            }
        }
    }

    private fun endJobIsCurrent(
        endedSnapshot: QueueSnapshot,
        endedPosition: Long?,
    ): Boolean =
        snapshot?.version == endedSnapshot.version &&
            snapshot?.resetVersion == endedSnapshot.resetVersion &&
            queueRepository.contentVersion() == endedSnapshot.version &&
            player?.isEnded == true &&
            currentQueuePosition() == endedPosition

    private fun Int.toRepeatMode(): RepeatMode = RepeatMode.entries.getOrElse(this) { RepeatMode.Off }

    private suspend fun QueueMutationScope.readSnapshot(): QueueSnapshot {
        var snapshot = snapshot()
        if (!snapshot.shuffled) {
            if (shuffleEnabled && snapshot.size > 0L) {
                setShuffle(null)
                snapshot = snapshot()
            }
            shuffleEnabled = false
        }
        return snapshot
    }

    private fun QueueWindowItem.playerItem(snapshot: QueueSnapshot): QueueItem =
        item.toQueueItem(albumContext = snapshot.isAlbumContext(position)).copy(resetItem = resetItem)

    private suspend fun queueItemAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): QueueItem? = queueRepository.range(snapshot, position, position).firstOrNull()?.playerItem(snapshot)

    private fun contextAt(position: Long?): QueueOrigin? {
        val snapshot = snapshot ?: return null
        val play = position?.let { snapshot.anchorContextPlay(it) } ?: return null
        val entry = snapshot.locateContext(play)?.first ?: return null
        return entry.origin
    }

    suspend fun sourceTitle(context: QueueOrigin?): String? = context?.let { queueRepository.sourceName(it.kind, it.sourceId, it.refId) }

    private fun updateBuffering() {
        val player = player
        if (player?.isBuffering != true) {
            bufferingJob?.cancel()
            bufferingJob = null
            stallJob?.cancel()
            stallJob = null
            stalled = false
            showBuffering = false
            return
        }
        if (!showBuffering && bufferingJob?.isActive != true) {
            bufferingJob =
                scope.launch {
                    delay(BUFFERING_INDICATOR_DELAY_MS)
                    if (player.isBuffering) {
                        showBuffering = true
                        refresh()
                    }
                }
        }
        // A stall is buffering that persists long enough to be a real problem, not a track change.
        if (stallJob?.isActive != true && !stalled) {
            stallJob =
                scope.launch {
                    delay(STALL_WATCHDOG_MS)
                    if (player === this@PlaybackController.player && player.isBuffering && player.playWhenReady) {
                        stalled = true
                        val item = player.currentItem
                        Log.w(
                            "playback",
                            "stalled: buffering for ${STALL_WATCHDOG_MS / 1000}s at ${player.currentPositionMs}ms " +
                                "item id=${item?.id} title=\"${item?.title}\"",
                        )
                    }
                }
        }
    }

    private fun refresh(position: Long? = currentQueuePosition()) {
        val player = player ?: return
        updateBuffering()
        val durationMs =
            player.durationMs.takeIf { it > 0 }
                ?: player.currentItem?.durationMs?.takeIf { it > 0 }
                ?: 0L
        _state.value =
            PlaybackState(
                item = player.currentItem,
                context = contextAt(position),
                position = position,
                isBuffering = showBuffering,
                isPlaying = player.playWhenReady && !player.isIdle && !player.isEnded,
                durationMs = durationMs,
                hasNext = true,
                hasPrevious = true,
                shuffle = shuffleEnabled,
                repeat = repeatMode,
                layout = layoutVersion,
            )
        _positionMs.value = player.currentPositionMs
        if (player.isPlaying) startPositionTicker() else stopPositionTicker()
    }

    private fun startPositionTicker() {
        if (positionJob?.isActive == true) return
        positionJob =
            scope.launch {
                while (true) {
                    delay(POSITION_TICK_MS)
                    val player = player ?: break
                    val positionMs = player.currentPositionMs
                    if (player.currentItem?.id == _state.value.item?.id) {
                        _positionMs.value = positionMs
                    }
                    if (shouldPersistPosition() &&
                        (positionMs - lastSavedPositionMs >= POSITION_SAVE_INTERVAL_MS || positionMs < lastSavedPositionMs)
                    ) {
                        persistPosition(positionMs)
                    }
                }
            }
    }

    private fun shouldPersistPosition(): Boolean = !detachedWindow || currentPosition() != null

    private fun persistPosition(positionMs: Long) {
        scope.launch {
            queueRepository.withMutationLock {
                if (!shouldPersistPosition()) return@withMutationLock
                lastSavedPositionMs = positionMs
                queueRepository.setPosition(positionMs)
            }
        }
    }

    private fun savePosition() {
        val player = player ?: return
        persistPosition(player.currentPositionMs)
    }

    private fun stopPositionTicker() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun PlayerHandle.ensurePrepared() {
        if (isIdle) prepare()
    }

    private companion object {
        const val POSITION_TICK_MS = 500L
        const val POSITION_SAVE_INTERVAL_MS = 15_000L
        const val BUFFERING_INDICATOR_DELAY_MS = 1_000L
        const val STALL_WATCHDOG_MS = 15_000L
        const val QUEUE_WINDOW_RADIUS = 25L
        const val WINDOW_SHIFT_DELAY_MS = 400L
        const val RESTART_THRESHOLD_MS = 3_000L
        const val SEED_PIN_TRIES = 1_000_000
        const val FILE_SCHEME = "file:"
        const val DETACHED_POSITION = -1L

        // The offline scan walks the queue one row at a time to find the next downloaded track, so
        // this caps the work on a sparse queue.
        const val OFFLINE_SCAN_LIMIT = 10_000L
    }
}
