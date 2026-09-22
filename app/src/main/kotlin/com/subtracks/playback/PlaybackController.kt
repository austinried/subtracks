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
    val error: String? = null,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)

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
        player?.seekTo(positionMs)
    }

    fun coverArt(
        item: QueueItem?,
        thumbnail: Boolean = false,
    ): CoverArtRef? = sourceRepository.coverArt(item?.coverArtId, thumbnail)

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
    ) {
        val player = player ?: return
        val snapshot = snapshot ?: return
        val window = queueRepository.window(snapshot, position, QUEUE_WINDOW_RADIUS)
        val startIndex = window.indexOfFirst { it.position == position }
        if (startIndex < 0) return
        windowStart = window.first().position
        windowEnd = window.last().position
        updating = true
        player.setWindow(window.map { it.item.toQueueItem() }, startIndex)
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

    private suspend fun shiftWindow(center: Long) =
        startLock.withLock {
            val player = player ?: return@withLock
            val snapshot = queueRepository.snapshot()
            this.snapshot = snapshot
            if (snapshot.size == 0L) return@withLock
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
