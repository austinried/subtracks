package com.subtracks.playback

import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueSnapshot
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    val refId: String,
)

data class PlaybackState(
    val item: QueueItem? = null,
    val context: QueueContext? = null,
    val position: Long? = null,
    val error: String? = null,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)

private sealed interface QueueUndo {
    val entries: List<QueueEntry>
    val cursor: Long
    val current: Long?
}

private data class RemovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val position: Long,
    val item: QueueItem?,
) : QueueUndo

private data class MovedUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
    override val current: Long,
    val from: Long,
    val to: Long,
) : QueueUndo

private data class ReloadUndo(
    override val entries: List<QueueEntry>,
    override val cursor: Long,
) : QueueUndo {
    override val current: Long? = null
}

class PlaybackController(
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
    private val connection: PlayerConnection,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    private var player: PlayerHandle? = null
    private var connecting = false
    private var pendingPlay: Pair<List<QueueEntry>, Long>? = null
    private var positionJob: Job? = null
    private var windowJob: Job? = null
    private var bufferingJob: Job? = null
    private var showBuffering = false
    private val startLock = Mutex()

    private var snapshot: QueueSnapshot? = null
    private var lastDurationMs = 0L
    private var lastError: String? = null
    private var queueSourceId: Long? = null
    private var windowStart = 0L
    private var windowEnd = -1L
    private var updating = false
    private var lastEdit: QueueUndo? = null

    init {
        scope.launch {
            sourceRepository.activeSourceId().collect { sourceId ->
                if (queueSourceId != null && sourceId != null && sourceId != queueSourceId) stop()
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
                    if (pending != null) start(pending.first, pending.second) else restore()
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

    fun playSongs(
        sourceId: Long,
        startPosition: Long,
    ) = play(listOf(queueRepository.songsEntry(sourceId)), startPosition)

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
                lastEdit = ReloadUndo(entries, cursor)
                return@withLock
            }
            val current = currentPosition() ?: return@withLock
            val target = (if (position < current) current - 1 else current).coerceIn(0, snapshot.size - 2)
            val playing = player.playWhenReady
            val removed = if (position == current) null else queueRepository.itemAt(snapshot, position)?.toQueueItem()
            queueRepository.removeAt(snapshot, position)
            this.snapshot = queueRepository.snapshot()
            windowJob?.cancel()
            lastEdit =
                if (removed == null) {
                    ReloadUndo(entries, cursor)
                } else {
                    RemovedUndo(entries, cursor, target, position, removed)
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
        if (!queueRepository.move(from, to)) return@withLock
        this.snapshot = queueRepository.snapshot()
        val target = movedCursor(current, from, to)
        windowJob?.cancel()
        val fromInWindow = from in windowStart..windowEnd
        val toInWindow = to in windowStart..windowEnd
        lastEdit =
            if (fromInWindow == toInWindow) {
                MovedUndo(entries, cursor, target, from, to)
            } else {
                ReloadUndo(entries, cursor)
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
            queueRepository.replace(undo.entries)
            val restored = queueRepository.snapshot()
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
        if (player.playWhenReady) player.pause() else player.play()
    }

    fun next() = skip(1)

    fun previous() = skip(-1)

    fun seekTo(positionMs: Long) {
        val player = player ?: return
        player.ensurePrepared()
        player.seekTo(positionMs)
        player.play()
    }

    fun coverArt(
        item: QueueItem?,
        thumbnail: Boolean = false,
    ): CoverArtRef? = sourceRepository.coverArt(item?.coverArtId, thumbnail)

    suspend fun upcomingCoverArt(thumbnail: Boolean = false): CoverArtRef? {
        val snap = snapshot ?: return null
        val position = currentPosition() ?: return null
        val next = queueRepository.itemAt(snap, position + 1) ?: return null
        return sourceRepository.coverArt(next.toQueueItem().coverArtId, thumbnail)
    }

    private fun play(
        entries: List<QueueEntry>,
        startPosition: Long,
    ) {
        if (player == null) {
            pendingPlay = entries to startPosition
            connect()
            return
        }
        scope.launch { start(entries, startPosition) }
    }

    private suspend fun start(
        entries: List<QueueEntry>,
        startPosition: Long,
    ) = startLock.withLock {
        windowJob?.cancel()
        windowStart = 0
        windowEnd = -1
        lastDurationMs = 0
        lastError = null
        lastEdit = null
        queueRepository.replace(entries)
        val snapshot = queueRepository.snapshot()
        this.snapshot = snapshot
        queueSourceId = entries.firstOrNull()?.sourceId
        if (snapshot.size == 0L) {
            stopLocked()
            return@withLock
        }
        val position = startPosition.coerceIn(0, snapshot.size - 1)
        queueRepository.setCursor(position)
        loadWindow(position, autoplay = true)
    }

    private suspend fun restore() =
        startLock.withLock {
            windowJob?.cancel()
            windowStart = 0
            windowEnd = -1
            val snapshot = queueRepository.snapshot()
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
            loadWindow(position, autoplay = false)
        }

    private suspend fun stop() = startLock.withLock { stopLocked() }

    private fun stopLocked() {
        windowJob?.cancel()
        bufferingJob?.cancel()
        bufferingJob = null
        showBuffering = false
        snapshot = null
        lastDurationMs = 0
        lastError = null
        queueSourceId = null
        windowStart = 0
        windowEnd = -1
        lastEdit = null
        player?.run {
            stop()
            clear()
        }
        _state.value = PlaybackState()
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

    private fun skip(delta: Long) {
        scope.launch {
            val current = currentPosition() ?: return@launch
            val snapshot = queueRepository.snapshot()
            this@PlaybackController.snapshot = snapshot
            if (snapshot.size == 0L) return@launch
            val position = current.coerceIn(0, snapshot.size - 1)
            val target = (position + delta).coerceIn(0, snapshot.size - 1)
            if (target == position) return@launch
            windowJob?.cancel()
            queueRepository.setCursor(target)
            shiftWindow(target)
            val player = player ?: return@launch
            player.ensurePrepared()
            player.seekToIndex((target - windowStart).toInt())
            refresh(target)
        }
    }

    private suspend fun onPositionChanged() {
        val player = player ?: return
        val snapshot = snapshot ?: return
        if (snapshot.size == 0L) return
        val position = (windowStart + player.currentIndex).coerceIn(0, snapshot.size - 1)
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
        val snapshot = queueRepository.snapshot()
        this.snapshot = snapshot
        if (snapshot.size == 0L) return
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
                lastError = null
                if (updating) return
                scope.launch { onPositionChanged() }
            }

            override fun onEvents() = refresh()

            override fun onError(message: String) {
                lastError = message
                refresh()
            }
        }

    private fun contextAt(position: Long?): QueueContext? {
        val entry = position?.let { snapshot?.locate(it)?.first } ?: return null
        return QueueContext(entry.kind, entry.refId)
    }

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
        val size = snapshot?.size ?: 0
        lastDurationMs = player.durationMs.takeIf { it > 0 } ?: lastDurationMs
        _state.value =
            PlaybackState(
                item = player.currentItem,
                context = contextAt(position),
                position = position,
                error = lastError,
                isBuffering = showBuffering,
                isPlaying = player.playWhenReady && !player.isIdle && !player.isEnded,
                positionMs = player.currentPositionMs,
                durationMs = lastDurationMs,
                hasNext = position != null && position < size - 1,
                hasPrevious = position != null && position > 0,
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
                    _state.value = _state.value.copy(positionMs = player.currentPositionMs)
                }
            }
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
        const val BUFFERING_INDICATOR_DELAY_MS = 1_000L
        const val QUEUE_WINDOW_RADIUS = 25L
        const val WINDOW_SHIFT_DELAY_MS = 400L
    }
}
