package com.subtracks.playback

import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueSnapshot
import com.subtracks.data.repo.QueueWindowItem
import com.subtracks.data.repo.Shuffle
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

data class QueueItem(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val coverArtId: String?,
    val durationMs: Long? = null,
)

data class QueueContext(
    val kind: QueueKind,
    val sourceId: Long,
    val refId: String,
)

enum class RepeatMode { Off, All, One }

data class PlaybackState(
    val item: QueueItem? = null,
    val context: QueueContext? = null,
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
) : QueueUndo

private data class ReloadUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val shuffleSeed: Long?,
    override val upNext: List<QueueEntry>,
    override val upNextAnchor: Long,
) : QueueUndo {
    override val current: Long? = null
}

private data class PendingPlay(
    val entries: List<QueueEntry>,
    val position: Long,
    val disableShuffle: Boolean,
)

class PlaybackController(
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
    private val connection: PlayerConnection,
    private val showMessage: (String) -> Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs

    private var player: PlayerHandle? = null
    private var connecting = false
    private var pendingPlay: PendingPlay? = null
    private var pendingShuffle: QueueEntry? = null
    private var positionJob: Job? = null
    private var windowJob: Job? = null
    private var bufferingJob: Job? = null
    private var showBuffering = false
    private val startLock = Mutex()

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

    init {
        scope.launch {
            sourceRepository.activeSourceId().collect { sourceId ->
                if (queueSourceId != null && sourceId != null && sourceId != queueSourceId) stop()
            }
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

    fun close() {
        scope.cancel()
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
                        pending != null -> start(pending.entries, pending.position, pending.disableShuffle)
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
    ) = play(listOf(queueRepository.albumEntry(sourceId, albumId)), startOrdinal)

    fun playPlaylist(
        sourceId: Long,
        playlistId: String,
        startPosition: Long,
    ) = play(listOf(queueRepository.playlistEntry(sourceId, playlistId)), startPosition)

    fun playAlbumInOrder(
        sourceId: Long,
        albumId: String,
    ) = play(listOf(queueRepository.albumEntry(sourceId, albumId)), 0, disableShuffle = true)

    fun playPlaylistInOrder(
        sourceId: Long,
        playlistId: String,
    ) = play(listOf(queueRepository.playlistEntry(sourceId, playlistId)), 0, disableShuffle = true)

    fun shuffleAlbum(
        sourceId: Long,
        albumId: String,
    ) = shufflePlay(queueRepository.albumEntry(sourceId, albumId))

    fun shufflePlaylist(
        sourceId: Long,
        playlistId: String,
    ) = shufflePlay(queueRepository.playlistEntry(sourceId, playlistId))

    private fun shufflePlay(entry: QueueEntry) {
        if (player == null) {
            pendingShuffle = entry
            connect()
            return
        }
        scope.launch { startShuffled(entry) }
    }

    private suspend fun startShuffled(entry: QueueEntry) =
        startLock.withLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            lastEdit = null
            endedHandled = false
            val previous = snapshot
            val previousEntry = previous?.entries?.firstOrNull()?.entry
            val same = previousEntry != null && previousEntry.kind == entry.kind && previousEntry.refId == entry.refId
            val avoid = if (same) currentPosition()?.let { previous.flatContext(previous.anchorContextPlay(it)) } else null
            queueRepository.replace(listOf(entry))
            var snap = queueRepository.snapshot()
            if (snap.contextSize == 0L) {
                stopLocked()
                return@withLock
            }
            val start = randomIndex(snap.contextSize, avoid)
            queueRepository.setShuffle(pinnedSeed(snap.contextSize, start))
            shuffleEnabled = true
            queueSourceId = entry.sourceId
            queueRepository.setCursor(0)
            snap = queueRepository.snapshot()
            this@PlaybackController.snapshot = snap
            loadWindow(0, autoplay = true)
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

    fun playAt(position: Long) {
        scope.launch {
            startLock.withLock {
                val player = player ?: return@withLock
                var snapshot = snapshot ?: return@withLock
                if (snapshot.size == 0L) return@withLock
                val anchor = queueRepository.reanchorFor(snapshot, position)
                if (anchor.changed) {
                    snapshot = readSnapshot()
                    this@PlaybackController.snapshot = snapshot
                    layoutVersion++
                }
                val target = anchor.position.coerceIn(0, snapshot.size - 1)
                windowJob?.cancel()
                queueRepository.setCursor(target)
                if (anchor.changed) {
                    loadWindow(target, autoplay = true)
                } else if (target in windowStart..windowEnd) {
                    player.ensurePrepared()
                    lastPosition = target
                    player.seekToIndex((target - windowStart).toInt())
                    player.play()
                    shiftWindowLocked(target)
                    refresh(target)
                } else {
                    loadWindow(target, autoplay = true)
                }
            }
        }
    }

    suspend fun removeAt(position: Long) =
        startLock.withLock {
            val snapshot = snapshot ?: return@withLock
            if (position !in 0 until snapshot.size) return@withLock
            val player = player ?: return@withLock
            val cursor = queueRepository.cursor()
            val entries = snapshot.entries.map { it.entry }
            val upNext = snapshot.upNext.map { it.entry }
            if (snapshot.size == 1L) {
                queueRepository.removeAt(snapshot, position)
                stopLocked()
                lastEdit = ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
                return@withLock
            }
            val current = currentPosition() ?: return@withLock
            val playing = player.playWhenReady
            val wasUpNext = snapshot.isUpNext(current)
            val removed = if (position == current) null else queueRepository.itemAt(snapshot, position)?.toQueueItem()
            queueRepository.removeAt(snapshot, position)
            var updated = readSnapshot()
            this.snapshot = updated
            windowJob?.cancel()
            if (updated.size == 0L) {
                stopLocked()
                lastEdit = ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
                return@withLock
            }
            // The block floats behind the currently playing context track, so removing that track
            // removes its anchor. Re-anchor the block to the track that now precedes the removed
            // slot in play order, so it takes the slot and plays next, rather than the anchor
            // landing wherever `adjustAnchorOnRemove` left it.
            if (snapshot.shuffled && position == current && !wasUpNext && updated.upNextSize > 0L) {
                val play = snapshot.contextPlay(current)
                val predecessor = play - 1L
                val anchor = if (predecessor < 0L) -1L else updated.flatContext(predecessor) ?: -1L
                queueRepository.setUpNextAnchor(anchor)
                updated = readSnapshot()
                this.snapshot = updated
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
                val reanchored = queueRepository.reanchorFor(updated, target)
                if (reanchored.changed) {
                    updated = readSnapshot()
                    this.snapshot = updated
                    target = reanchored.position
                }
            }
            lastPosition = target
            lastEdit =
                if (removed == null) {
                    ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
                } else {
                    RemovedUndo(entries, cursor, target, position, removed, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
                }
            if (snapshot.upNextSize > 0L || updated.upNextSize > 0L || snapshot.shuffled) {
                queueRepository.setCursor(target)
                if (position != current) {
                    rebuildWindow(queueRepository.window(updated, target, QUEUE_WINDOW_RADIUS), target)
                    refresh(target)
                } else {
                    loadWindow(target, autoplay = playing, startPositionMs = 0)
                }
                return@withLock
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

    suspend fun move(
        from: Long,
        to: Long,
    ) = startLock.withLock {
        if (from == to) return@withLock
        val snapshot = snapshot ?: return@withLock
        if (from !in 0 until snapshot.size || to !in 0 until snapshot.size) return@withLock
        val player = player ?: return@withLock
        val current = currentPosition() ?: return@withLock
        val cursor = queueRepository.cursor()
        val entries = snapshot.entries.map { it.entry }
        val upNext = snapshot.upNext.map { it.entry }
        val wasUpNext = snapshot.isUpNext(current)
        if (!queueRepository.move(snapshot, from, to)) return@withLock
        val updated = readSnapshot()
        this.snapshot = updated
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
                MovedUndo(entries, cursor, target, from, to, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
            } else {
                ReloadUndo(entries, cursor, snapshot.shuffleSeed, upNext, snapshot.upNextAnchor)
            }
        when {
            fromInWindow && toInWindow -> {
                updating = true
                player.move((from - windowStart).toInt(), (to - windowStart).toInt())
                updating = false
            }

            !fromInWindow && !toInWindow -> {
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

    suspend fun undo() =
        startLock.withLock {
            val undo = lastEdit ?: return@withLock
            val player = player ?: return@withLock
            val playing = player.playWhenReady
            val positionMs = player.currentPositionMs
            queueRepository.replace(undo.entries, undo.shuffleSeed, undo.upNext, undo.upNextAnchor)
            shuffleEnabled = undo.shuffleSeed != null
            val restored = readSnapshot()
            this.snapshot = restored
            lastEdit = null
            if (restored.size == 0L) {
                stopLocked()
                return@withLock
            }
            val target = undo.cursor.coerceIn(0, restored.size - 1)
            lastPosition = target
            val restoredId = queueRepository.itemAt(restored, target)?.song?.id
            windowJob?.cancel()
            val canMirror =
                undo.current != null &&
                    player.currentItem?.id != null &&
                    player.currentItem?.id == restoredId &&
                    currentPosition() == undo.current
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
        val snapshot = snapshot ?: return
        if (snapshot.size == 0L) return
        val current = currentPosition() ?: return
        if (snapshot.isUpNext(current)) {
            scope.launch { consumeUpNext(current) }
            return
        }
        when {
            current < snapshot.size - 1 -> jumpTo(current + 1)
            repeatMode != RepeatMode.Off -> jumpTo(0)
        }
    }

    private suspend fun consumeUpNext(position: Long) =
        startLock.withLock {
            val snapshot = snapshot ?: return@withLock
            if (!snapshot.isUpNext(position)) return@withLock
            if (position >= snapshot.size - 1 && repeatMode == RepeatMode.Off) return@withLock
            queueRepository.removeUpNextAt(snapshot, snapshot.upNextIndex(position))
            val updated = readSnapshot()
            this.snapshot = updated
            layoutVersion++
            windowJob?.cancel()
            endedHandled = false
            val next = if (position < updated.size) position else 0L
            queueRepository.setCursor(next)
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
            startLock.withLock {
                val snapshot = snapshot ?: return@withLock
                if (snapshot.upNextSize == 0L) return@withLock
                player ?: return@withLock
                val current = currentPosition() ?: return@withLock
                val wasUpNext = snapshot.isUpNext(current)
                queueRepository.clearUpNext()
                val updated = readSnapshot()
                this@PlaybackController.snapshot = updated
                layoutVersion++
                windowJob?.cancel()
                endedHandled = false
                val contextPlay = if (wasUpNext) snapshot.anchorPlay + 1 else snapshot.contextPlay(current)
                val target = contextPlay.coerceIn(0, (updated.size - 1).coerceAtLeast(0))
                queueRepository.setCursor(target)
                lastPosition = target
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
        val player = player ?: return
        if (player.currentPositionMs > RESTART_THRESHOLD_MS) {
            scope.launch {
                startLock.withLock { player.seekTo(0) }
                refresh()
            }
            return
        }
        val snapshot = snapshot ?: return
        if (snapshot.size == 0L) return
        val current = currentPosition() ?: return
        when {
            current > 0 -> {
                jumpTo(current - 1)
            }

            repeatMode == RepeatMode.All -> {
                jumpTo(snapshot.size - 1)
            }

            else -> {
                player.seekTo(0)
                refresh()
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
    ) = startLock.withLock {
        val snapshot = snapshot ?: return@withLock
        val player = player ?: return@withLock
        if (snapshot.size == 0L) return@withLock
        val current = currentPosition() ?: return@withLock
        val position = queueRepository.addUpNext(snapshot, current, entry, playNext)
        val updated = readSnapshot()
        this.snapshot = updated
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
            startLock.withLock {
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withLock
                player ?: return@withLock
                val current = currentPosition() ?: return@withLock
                val wasUpNext = snapshot.isUpNext(current)
                val blockIndex = snapshot.upNextIndex(current)
                val flat = snapshot.flatContext(snapshot.anchorContextPlay(current)) ?: return@withLock
                windowJob?.cancel()
                val enabling = !shuffleEnabled
                if (enabling) {
                    queueRepository.setShuffle(pinnedSeed(snapshot.contextSize, flat))
                } else {
                    queueRepository.setShuffle(null)
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
                val anchor = queueRepository.reanchorFor(reordered, position)
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
        lastSavedPositionMs = positionMs
        scope.launch { queueRepository.setPosition(positionMs) }
    }

    fun coverArt(
        item: QueueItem?,
        thumbnail: Boolean = false,
    ): CoverArtRef? = sourceRepository.coverArt(item?.coverArtId, thumbnail)

    suspend fun upcomingItem(): QueueItem? {
        val snap = snapshot ?: return null
        val position = currentPosition() ?: return null
        return queueRepository.itemAt(snap, position + 1)?.toQueueItem()
    }

    private fun play(
        entries: List<QueueEntry>,
        startPosition: Long,
        disableShuffle: Boolean = false,
    ) {
        if (player == null) {
            pendingPlay = PendingPlay(entries, startPosition, disableShuffle)
            connect()
            return
        }
        scope.launch { start(entries, startPosition, disableShuffle) }
    }

    private suspend fun start(
        entries: List<QueueEntry>,
        startPosition: Long,
        disableShuffle: Boolean = false,
    ) = startLock.withLock {
        windowJob?.cancel()
        windowStart = 0
        windowEnd = -1
        lastEdit = null
        endedHandled = false
        val modes = queueRepository.modes()
        shuffleEnabled = !disableShuffle && modes.shuffle
        repeatMode = modes.repeat.toRepeatMode()
        player?.setRepeatOne(repeatMode == RepeatMode.One)
        queueRepository.replace(entries)
        val snapshot = queueRepository.snapshot()
        this.snapshot = snapshot
        queueSourceId = entries.firstOrNull()?.sourceId
        if (snapshot.size == 0L) {
            stopLocked()
            return@withLock
        }
        val start = startPosition.coerceIn(0, snapshot.contextSize - 1)
        val position =
            if (shuffleEnabled) {
                queueRepository.setShuffle(pinnedSeed(snapshot.contextSize, start))
                0L
            } else {
                start
            }
        queueRepository.setCursor(position)
        this.snapshot = readSnapshot()
        loadWindow(position, autoplay = true)
    }

    private suspend fun restore() =
        startLock.withLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            endedHandled = false
            val modes = queueRepository.modes()
            shuffleEnabled = modes.shuffle
            repeatMode = modes.repeat.toRepeatMode()
            player?.setRepeatOne(repeatMode == RepeatMode.One)
            var snapshot = queueRepository.snapshot()
            if (shuffleEnabled && !snapshot.shuffled && snapshot.contextSize > 0L) {
                val start = snapshot.anchorContextPlay(queueRepository.cursor()).coerceIn(0, snapshot.contextSize - 1)
                val flat = snapshot.flatContext(start) ?: start
                queueRepository.setShuffle(pinnedSeed(snapshot.contextSize, flat))
                queueRepository.setCursor(0)
                snapshot = queueRepository.snapshot()
            } else if (!snapshot.shuffled) {
                shuffleEnabled = false
            }
            this.snapshot = snapshot
            val queueSourceId =
                snapshot.entries
                    .firstOrNull()
                    ?.entry
                    ?.sourceId
            val activeId = sourceRepository.activeSourceIdOnce()
            if (snapshot.size == 0L || (queueSourceId != null && activeId != null && queueSourceId != activeId)) {
                stopLocked()
                return@withLock
            }
            this.queueSourceId = queueSourceId
            val savedPositionMs = queueRepository.cursorPositionMs().coerceAtLeast(0)
            val cursor = queueRepository.cursor().coerceIn(0, snapshot.size - 1)
            val anchor = queueRepository.reanchorFor(snapshot, cursor)
            if (anchor.changed) {
                snapshot = queueRepository.snapshot()
                this.snapshot = snapshot
                queueRepository.setCursor(anchor.position)
                queueRepository.setPosition(savedPositionMs)
            }
            lastSavedPositionMs = savedPositionMs
            loadWindow(anchor.position, autoplay = false, startPositionMs = savedPositionMs)
        }

    private suspend fun stop() = startLock.withLock { stopLocked() }

    private fun stopLocked() {
        windowJob?.cancel()
        bufferingJob?.cancel()
        bufferingJob = null
        showBuffering = false
        snapshot = null
        queueSourceId = null
        windowStart = 0
        windowEnd = -1
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
        val window = queueRepository.window(snapshot, position, QUEUE_WINDOW_RADIUS)
        val startIndex = window.indexOfFirst { it.position == position }
        if (startIndex < 0) return
        windowStart = window.first().position
        windowEnd = window.last().position
        lastPosition = position
        updating = true
        player.setWindow(window.map { it.item.toQueueItem() }, startIndex, startPositionMs)
        updating = false
        if (autoplay) {
            player.prepare()
            player.play()
        }
        refresh(position)
    }

    private suspend fun reloadQuality() =
        startLock.withLock {
            val player = player ?: return@withLock
            val snapshot = snapshot ?: return@withLock
            if (snapshot.size == 0L) return@withLock
            val sourceId = queueSourceId
            if (sourceId == null || sourceRepository.activeSourceIdOnce() != sourceId) return@withLock
            if (player.isEnded) return@withLock
            val index = player.currentIndex
            val position = (windowStart + index).coerceIn(0, snapshot.size - 1)
            val positionMs = player.currentPositionMs
            if (player.currentIndex != index) return@withLock
            loadWindow(position, autoplay = player.playWhenReady, startPositionMs = positionMs)
        }

    private fun jumpTo(target: Long) {
        scope.launch {
            startLock.withLock {
                val player = player ?: return@withLock
                var snapshot = readSnapshot()
                if (snapshot.size == 0L) return@withLock
                val anchor = queueRepository.reanchorFor(snapshot, target)
                if (anchor.changed) {
                    snapshot = readSnapshot()
                    layoutVersion++
                }
                this@PlaybackController.snapshot = snapshot
                val current = currentPosition() ?: return@withLock
                val dest = anchor.position.coerceIn(0, snapshot.size - 1)
                val wasEnded = player.isEnded
                if (!anchor.changed && dest == current && !wasEnded) return@withLock
                windowJob?.cancel()
                queueRepository.setCursor(dest)
                if (anchor.changed) {
                    loadWindow(dest, autoplay = player.playWhenReady || wasEnded)
                    endedHandled = false
                    return@withLock
                }
                lastPosition = dest
                shiftWindowLocked(dest)
                player.ensurePrepared()
                player.seekToIndex((dest - windowStart).toInt())
                if (wasEnded) player.play()
                endedHandled = false
                refresh(dest)
            }
        }
    }

    private suspend fun onPositionChanged() {
        val player = player ?: return
        val snapshot = snapshot ?: return
        if (snapshot.size == 0L) return
        val position = (windowStart + player.currentIndex).coerceIn(0, snapshot.size - 1)
        val previous = lastPosition
        lastPosition = position
        if (previous != null && previous != position && snapshot.isUpNext(previous)) {
            consumeUpNext(previous)
            return
        }
        lastSavedPositionMs = 0
        queueRepository.setCursor(position)
        refresh(position)
        scheduleWindowShift(position)
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

    private suspend fun shiftWindow(center: Long) = startLock.withLock { shiftWindowLocked(center) }

    private suspend fun shiftWindowLocked(center: Long) {
        val player = player ?: return
        val previous = snapshot
        val snapshot = readSnapshot()
        this.snapshot = snapshot
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
                val item = queueRepository.itemAt(snapshot, windowStart - 1) ?: break
                player.addFirst(item.toQueueItem())
                windowStart--
            }
            while (windowStart < desiredStart) {
                player.removeFirst()
                windowStart++
            }
            while (windowEnd < desiredEnd) {
                val item = queueRepository.itemAt(snapshot, windowEnd + 1) ?: break
                player.addLast(item.toQueueItem())
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

    private fun rebuildWindow(
        window: List<QueueWindowItem>,
        position: Long,
    ) {
        val player = player ?: return
        val index = window.indexOfFirst { it.position == position }
        if (index < 0) return
        updating = true
        try {
            repeat(player.currentIndex) { player.removeFirst() }
            while (player.itemCount > 1) player.removeLast()
            for (i in index - 1 downTo 0) player.addFirst(window[i].item.toQueueItem())
            for (i in index + 1 until window.size) player.addLast(window[i].item.toQueueItem())
        } finally {
            updating = false
        }
        windowStart = window.first().position
        windowEnd = window.last().position
        lastPosition = position
    }

    private fun currentPosition(): Long? {
        val player = player ?: return null
        if (player.itemCount == 0) return null
        return windowStart + player.currentIndex
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

            override fun onError(message: String) {
                showMessage(message)
                refresh()
            }
        }

    private fun handleEnded() {
        if (endedHandled) return
        endedHandled = true
        if (repeatMode == RepeatMode.All) jumpTo(0)
    }

    private fun Int.toRepeatMode(): RepeatMode = RepeatMode.entries.getOrElse(this) { RepeatMode.Off }

    private suspend fun readSnapshot(): QueueSnapshot {
        val snapshot = queueRepository.snapshot()
        if (!snapshot.shuffled) {
            if (shuffleEnabled && snapshot.size > 0L) queueRepository.setShuffle(null)
            shuffleEnabled = false
        }
        return snapshot
    }

    private fun contextAt(position: Long?): QueueContext? {
        val snapshot = snapshot ?: return null
        val play = position?.let { snapshot.anchorContextPlay(it) } ?: return null
        val entry = snapshot.locateContext(play)?.first ?: return null
        return QueueContext(entry.kind, entry.sourceId, entry.refId)
    }

    suspend fun sourceTitle(context: QueueContext?): String? = context?.let { queueRepository.sourceName(it.kind, it.sourceId, it.refId) }

    private fun updateBuffering() {
        if (player?.isBuffering != true) {
            bufferingJob?.cancel()
            bufferingJob = null
            showBuffering = false
            return
        }
        if (showBuffering || bufferingJob?.isActive == true) return
        bufferingJob =
            scope.launch {
                delay(BUFFERING_INDICATOR_DELAY_MS)
                if (player?.isBuffering == true) {
                    showBuffering = true
                    refresh()
                }
            }
    }

    private fun refresh(position: Long? = currentPosition()) {
        val player = player ?: return
        updateBuffering()
        val durationMs =
            player.durationMs.takeIf { it > 0 }
                ?: player.currentItem?.durationMs?.takeIf { it > 0 }
                ?: 0L
        _positionMs.value = player.currentPositionMs
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
                    _positionMs.value = positionMs
                    if (positionMs - lastSavedPositionMs >= POSITION_SAVE_INTERVAL_MS || positionMs < lastSavedPositionMs) {
                        lastSavedPositionMs = positionMs
                        queueRepository.setPosition(positionMs)
                    }
                }
            }
    }

    private fun savePosition() {
        val player = player ?: return
        val positionMs = player.currentPositionMs
        lastSavedPositionMs = positionMs
        scope.launch { queueRepository.setPosition(positionMs) }
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
        const val QUEUE_WINDOW_RADIUS = 25L
        const val WINDOW_SHIFT_DELAY_MS = 400L
        const val RESTART_THRESHOLD_MS = 3_000L
        const val SEED_PIN_TRIES = 1_000_000
    }
}
