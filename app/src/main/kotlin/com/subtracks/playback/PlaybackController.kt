package com.subtracks.playback

import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueSnapshot
import com.subtracks.data.repo.QueueWindowItem
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
import kotlinx.coroutines.withContext
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
)

private sealed interface QueueUndo {
    val entries: List<QueueEntry>
    val cursor: Long
    val current: Long?
    val shuffleOrder: LongArray?
}

private data class RemovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val position: Long,
    val item: QueueItem?,
    override val shuffleOrder: LongArray?,
) : QueueUndo

private data class MovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val from: Long,
    val to: Long,
    override val shuffleOrder: LongArray?,
) : QueueUndo

private data class ReloadUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val shuffleOrder: LongArray?,
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
                }
            },
            onDisconnected = {
                player = null
                connecting = false
                scope.launch { stop() }
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
            val avoid = if (same) currentPosition()?.let { previous.flatPosition(it) } else null
            queueRepository.replace(listOf(entry))
            var snap = queueRepository.snapshot()
            if (snap.size == 0L) {
                stopLocked()
                return@withLock
            }
            val start = randomIndex(snap.size, avoid)
            val order = withContext(Dispatchers.Default) { shuffledOrder(snap.size, start) }
            queueRepository.setShuffle(true, order)
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

    fun playAt(position: Long) {
        scope.launch {
            startLock.withLock {
                val snapshot = snapshot ?: return@withLock
                if (snapshot.size == 0L) return@withLock
                val player = player ?: return@withLock
                val target = position.coerceIn(0, snapshot.size - 1)
                windowJob?.cancel()
                queueRepository.setCursor(target)
                if (target in windowStart..windowEnd) {
                    player.ensurePrepared()
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
            if (snapshot.size == 1L) {
                queueRepository.removeAt(snapshot, position)
                stopLocked()
                lastEdit = ReloadUndo(entries, cursor, snapshot.shuffleOrder)
                return@withLock
            }
            val current = currentPosition() ?: return@withLock
            val target = (if (position < current) current - 1 else current).coerceIn(0, snapshot.size - 2)
            val playing = player.playWhenReady
            val removed = if (position == current) null else queueRepository.itemAt(snapshot, position)?.toQueueItem()
            queueRepository.removeAt(snapshot, position)
            this.snapshot = readSnapshot()
            windowJob?.cancel()
            lastEdit =
                if (removed == null) {
                    ReloadUndo(entries, cursor, snapshot.shuffleOrder)
                } else {
                    RemovedUndo(entries, cursor, target, position, removed, snapshot.shuffleOrder)
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
        if (!queueRepository.move(snapshot, from, to)) return@withLock
        this.snapshot = readSnapshot()
        val target = movedCursor(current, from, to)
        windowJob?.cancel()
        val fromInWindow = from in windowStart..windowEnd
        val toInWindow = to in windowStart..windowEnd
        lastEdit =
            if (fromInWindow == toInWindow) {
                MovedUndo(entries, cursor, target, from, to, snapshot.shuffleOrder)
            } else {
                ReloadUndo(entries, cursor, snapshot.shuffleOrder)
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
                loadWindow(target, autoplay = player.playWhenReady, startPositionMs = player.currentPositionMs)
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
            queueRepository.replace(undo.entries, undo.shuffleOrder)
            shuffleEnabled = undo.shuffleOrder != null
            val restored = readSnapshot()
            this.snapshot = restored
            lastEdit = null
            if (restored.size == 0L) {
                stopLocked()
                return@withLock
            }
            val target = undo.cursor.coerceIn(0, restored.size - 1)
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
        when {
            current < snapshot.size - 1 -> jumpTo(current + 1)
            repeatMode != RepeatMode.Off -> jumpTo(0)
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
                val flat = snapshot.flatPosition(current) ?: return@withLock
                windowJob?.cancel()
                val position =
                    if (shuffleEnabled) {
                        queueRepository.setShuffle(false, null)
                        shuffleEnabled = false
                        flat
                    } else {
                        val order = withContext(Dispatchers.Default) { shuffledOrder(snapshot.size, flat) }
                        queueRepository.setShuffle(true, order)
                        shuffleEnabled = true
                        0L
                    }
                queueRepository.setCursor(position)
                val reordered = readSnapshot()
                this@PlaybackController.snapshot = reordered
                rebuildWindow(queueRepository.window(reordered, position, QUEUE_WINDOW_RADIUS), position)
                endedHandled = false
                refresh(position)
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
        val start = startPosition.coerceIn(0, snapshot.size - 1)
        val position =
            if (shuffleEnabled) {
                val order = withContext(Dispatchers.Default) { shuffledOrder(snapshot.size, start) }
                queueRepository.setShuffle(true, order)
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
            if (shuffleEnabled && !snapshot.shuffled && snapshot.size > 0L) {
                val start = queueRepository.cursor().coerceIn(0, snapshot.size - 1)
                val order = withContext(Dispatchers.Default) { shuffledOrder(snapshot.size, start) }
                queueRepository.setShuffle(true, order)
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
            val position = queueRepository.cursor().coerceIn(0, snapshot.size - 1)
            lastSavedPositionMs = queueRepository.cursorPositionMs().coerceAtLeast(0)
            loadWindow(position, autoplay = false, startPositionMs = lastSavedPositionMs)
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
                val snapshot = readSnapshot()
                this@PlaybackController.snapshot = snapshot
                if (snapshot.size == 0L) return@withLock
                val current = currentPosition() ?: return@withLock
                val dest = target.coerceIn(0, snapshot.size - 1)
                val wasEnded = player?.isEnded == true
                if (dest == current && !wasEnded) return@withLock
                windowJob?.cancel()
                queueRepository.setCursor(dest)
                shiftWindowLocked(dest)
                val player = player ?: return@withLock
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
                (currentId?.let { queueRepository.flatIndexOf(snapshot, it) } ?: center)
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

    private fun shuffledOrder(
        size: Long,
        first: Long,
    ): LongArray {
        val rest = (0 until size).filter { it != first }.shuffled()
        return (listOf(first) + rest).toLongArray()
    }

    private fun Int.toRepeatMode(): RepeatMode = RepeatMode.entries.getOrElse(this) { RepeatMode.Off }

    private suspend fun readSnapshot(): QueueSnapshot {
        val snapshot = queueRepository.snapshot()
        if (!snapshot.shuffled) {
            if (shuffleEnabled && snapshot.size > 0L) queueRepository.setShuffle(false, null)
            shuffleEnabled = false
        }
        return snapshot
    }

    private fun contextAt(position: Long?): QueueContext? {
        val entry = position?.let { snapshot?.locate(it)?.first } ?: return null
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
    }
}
