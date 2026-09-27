package com.subtracks.ui.playback

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.SongListItem
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.QUEUE_CHUNK
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueWindowItem
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.library.SongRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val LOAD_THRESHOLD = 5
private const val OLDER_LOAD = 30
private const val POSITION_TIMEOUT_MS = 1_000L

internal const val QUEUE_WINDOW_ROWS = QUEUE_CHUNK * 3

data class QueueRow(
    val id: Long,
    val position: Long,
    val song: SongListItem,
    val upNext: Boolean = false,
)

class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val rows = mutableStateListOf<QueueRow>()

    var ready by mutableStateOf(false)
        private set

    var initialIndex by mutableStateOf(0)
        private set

    var generation by mutableStateOf(0)
        private set

    private var nextId = 0L
    private val mutex = Mutex()

    private fun newRow(item: QueueWindowItem) = QueueRow(nextId++, item.position, item.item, item.upNext)

    private fun reusedIds(): Map<String, MutableList<Long>> =
        rows.groupBy { it.song.song.id }.mapValues { entry -> entry.value.map { it.id }.toMutableList() }

    private fun rowFor(
        item: QueueWindowItem,
        reused: Map<String, MutableList<Long>>,
    ): QueueRow {
        val ids = reused[item.item.song.id]
        val id = if (!ids.isNullOrEmpty()) ids.removeAt(0) else nextId++
        return QueueRow(id, item.position, item.item, item.upNext)
    }

    fun open() {
        viewModelScope.launch {
            mutex.withLock {
                val snapshot = queueRepository.snapshot()
                val reused = reusedIds()
                rows.clear()
                if (snapshot.size > 0L) {
                    val cursor =
                        withTimeoutOrNull(POSITION_TIMEOUT_MS) {
                            playbackController.state.first { it.position != null }.position
                        }?.coerceIn(0, snapshot.size - 1) ?: 0L
                    val start = (cursor - OLDER_LOAD).coerceAtLeast(0)
                    val end = (start + QUEUE_CHUNK - 1).coerceAtMost(snapshot.size - 1)
                    rows.addAll(queueRepository.range(snapshot, start, end).map { rowFor(it, reused) })
                    initialIndex = rows.indexOfFirst { it.position == cursor }.coerceAtLeast(0)
                }
                ready = true
                generation++
            }
        }
    }

    fun reconcile() {
        viewModelScope.launch { mutex.withLock { reconcileLocked() } }
    }

    private suspend fun reconcileLocked() {
        if (!ready) return
        val snapshot = queueRepository.snapshot()
        if (snapshot.size == 0L) {
            rows.clear()
            return
        }
        val start = (rows.firstOrNull()?.position ?: 0L).coerceIn(0, snapshot.size - 1)
        val end = (start + rows.size.coerceAtLeast(QUEUE_CHUNK) - 1).coerceAtMost(snapshot.size - 1)
        val loaded = queueRepository.range(snapshot, start, end)
        val reused = reusedIds()
        val updated = loaded.map { rowFor(it, reused) }
        if (updated.size == rows.size) {
            updated.forEachIndexed { index, row -> if (rows[index] != row) rows[index] = row }
        } else {
            rows.clear()
            rows.addAll(updated)
        }
    }

    fun loadOlder() {
        val top = rows.minOfOrNull { it.position } ?: return
        if (top <= 0L) return
        viewModelScope.launch {
            mutex.withLock {
                val first = rows.minOfOrNull { it.position } ?: return@withLock
                if (first <= 0L) return@withLock
                val snapshot = queueRepository.snapshot()
                val loaded = queueRepository.range(snapshot, (first - QUEUE_CHUNK).coerceAtLeast(0), first - 1)
                if (loaded.isEmpty()) return@withLock
                rows.addAll(0, loaded.map(::newRow))
                while (rows.size > QUEUE_WINDOW_ROWS) rows.removeAt(rows.size - 1)
            }
        }
    }

    fun loadNewer() {
        if (rows.isEmpty()) return
        viewModelScope.launch {
            mutex.withLock {
                val last = rows.maxOfOrNull { it.position } ?: return@withLock
                val snapshot = queueRepository.snapshot()
                if (last >= snapshot.size - 1) return@withLock
                val to = (last + QUEUE_CHUNK).coerceAtMost(snapshot.size - 1)
                val loaded = queueRepository.range(snapshot, last + 1, to)
                if (loaded.isEmpty()) return@withLock
                rows.addAll(loaded.map(::newRow))
                while (rows.size > QUEUE_WINDOW_ROWS) rows.removeAt(0)
            }
        }
    }

    fun reorder(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex == toIndex) return
        if (fromIndex !in rows.indices || toIndex !in rows.indices) return
        if (rows[fromIndex].upNext != rows[toIndex].upNext) return
        rows.add(toIndex, rows.removeAt(fromIndex))
    }

    fun play(position: Long) {
        viewModelScope.launch { playbackController.playAt(position) }
    }

    fun move(
        from: Long,
        to: Long,
    ) {
        if (from == to) return
        viewModelScope.launch {
            playbackController.move(from, to)
            mutex.withLock {
                val base = rows.minOfOrNull { it.position } ?: return@withLock
                // The loaded order already reflects the move, so only the positions need updating.
                val updated =
                    rows.mapIndexed { index, row ->
                        val position = base + index
                        if (row.position == position) row else row.copy(position = position)
                    }
                rows.clear()
                rows.addAll(updated)
            }
        }
    }

    fun remove(position: Long) {
        viewModelScope.launch {
            mutex.withLock {
                val index = rows.indexOfFirst { it.position == position }
                if (index >= 0) rows.removeAt(index)
                playbackController.removeAt(position)
                reconcileLocked()
            }
        }
    }

    fun undo() {
        viewModelScope.launch {
            playbackController.undo()
            mutex.withLock { reload() }
        }
    }

    private suspend fun reload() {
        val snapshot = queueRepository.snapshot()
        if (snapshot.size == 0L) {
            rows.clear()
            return
        }
        val start = (rows.firstOrNull()?.position ?: 0L).coerceIn(0, snapshot.size - 1)
        val end = (start + rows.size.coerceAtLeast(QUEUE_CHUNK) - 1).coerceAtMost(snapshot.size - 1)
        val loaded = queueRepository.range(snapshot, start, end)
        val reused = reusedIds()
        val updated = loaded.map { rowFor(it, reused) }
        rows.clear()
        rows.addAll(updated)
    }
}

@Composable
fun QueueRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    open: Boolean = true,
    viewModel: QueueViewModel = koinViewModel(),
    controller: PlaybackController = koinInject(),
    sourceRepository: SourceRepository = koinInject(),
    downloadRepository: DownloadRepository = koinInject(),
) {
    val playback by controller.state.collectAsStateWithLifecycle()
    val downloads by downloadRepository.states().collectAsStateWithLifecycle()
    val queueContext = playback.context
    // The item's album keeps the header stable while the async name lookup runs, so it does not
    // flash the fallback when tapping between context and manually queued tracks.
    val fallbackTitle = playback.item?.album?.takeIf { it.isNotBlank() } ?: "Next from here"
    val sourceTitle = remember(queueContext) { mutableStateOf(fallbackTitle) }
    LaunchedEffect(queueContext) {
        sourceTitle.value = controller.sourceTitle(queueContext)?.takeIf { it.isNotBlank() } ?: fallbackTitle
    }
    LaunchedEffect(playback.shuffle, open) { if (open) viewModel.open() }
    LaunchedEffect(playback.layout) { if (open) viewModel.reconcile() }
    QueueScreen(
        rows = viewModel.rows,
        ready = viewModel.ready,
        initialIndex = viewModel.initialIndex,
        generation = viewModel.generation,
        currentSongId = playback.item?.id,
        shuffle = playback.shuffle,
        contextTitle = sourceTitle.value,
        downloads = downloads,
        onClearUpNext = controller::clearUpNext,
        coverArt = sourceRepository::coverArt,
        onBack = onBack,
        onPlay = viewModel::play,
        onRemove = viewModel::remove,
        onReorder = viewModel::reorder,
        onMove = viewModel::move,
        onLoadOlder = viewModel::loadOlder,
        onLoadNewer = viewModel::loadNewer,
        onUndo = viewModel::undo,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    rows: List<QueueRow>,
    ready: Boolean,
    currentSongId: String?,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onPlay: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onMove: (Long, Long) -> Unit,
    onLoadOlder: () -> Unit,
    onLoadNewer: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    generation: Int = 0,
    shuffle: Boolean = false,
    contextTitle: String? = null,
    downloads: Map<String, SongDownload> = emptyMap(),
    onClearUpNext: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val fill = rememberViewportFill(listState)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dragId by remember { mutableStateOf<Long?>(null) }
    var dragFrom by remember { mutableStateOf<Long?>(null) }

    // One reorder state per run of rows: sh.calvin.reorderable only targets keys registered with
    // the state that started the drag, so a drag can never reach a row in another run. The context
    // sits on both sides of the up-next block, and splitting it there stops a context drag crossing
    // the block (which would move a track over the queued songs and relocate the anchor).
    // scrollThreshold 0 disables auto-scroll: the library keeps scrolling past a run's last valid
    // target with nowhere to drop, and the dragged item can be left off-screen. Scroll to where
    // you want to drop first, then drag.
    val upNextReorder =
        rememberReorderableLazyListState(listState, scrollThreshold = 0.dp) { from, to ->
            onReorder(rows.rowIndexOf(from.key), rows.rowIndexOf(to.key))
        }
    val contextBeforeReorder =
        rememberReorderableLazyListState(listState, scrollThreshold = 0.dp) { from, to ->
            onReorder(rows.rowIndexOf(from.key), rows.rowIndexOf(to.key))
        }
    val contextAfterReorder =
        rememberReorderableLazyListState(listState, scrollThreshold = 0.dp) { from, to ->
            onReorder(rows.rowIndexOf(from.key), rows.rowIndexOf(to.key))
        }

    LaunchedEffect(listState, ready, generation) {
        if (!ready) return@LaunchedEffect
        if (initialIndex > 0) listState.scrollToItem(rows.lazyIndexOf(initialIndex))
        snapshotFlow {
            val info = listState.layoutInfo
            val first = info.visibleItemsInfo.firstOrNull { it.key is Long }?.let { rows.rowIndexOf(it.key) } ?: -1
            val last = info.visibleItemsInfo.lastOrNull { it.key is Long }?.let { rows.rowIndexOf(it.key) } ?: -1
            first to last
        }.collect { (first, last) ->
            if (first in 0..LOAD_THRESHOLD) onLoadOlder()
            if (last >= 0 && last >= rows.size - 1 - LOAD_THRESHOLD) onLoadNewer()
        }
    }

    val showUndo: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(message, actionLabel = "Undo", duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) onUndo()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            !ready -> {
                LoadingState(Modifier.padding(padding))
            }

            rows.isEmpty() -> {
                EmptyState("The queue is empty.", Modifier.padding(padding))
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    val blockFirst = rows.indexOfFirst { it.upNext }
                    rows.forEachIndexed { index, row ->
                        if (index == 0 || rows[index - 1].upNext != row.upNext) {
                            // Keyed by index, not section: the context sits on both sides of the
                            // block, so a section-based key would duplicate.
                            item(key = "header-$index") {
                                QueueSectionHeader(
                                    label = if (row.upNext) "Up next" else contextTitle ?: "Next up",
                                    onClear = if (row.upNext) onClearUpNext else null,
                                )
                            }
                        }
                        val enabled = row.upNext || !shuffle
                        val section =
                            when {
                                row.upNext -> upNextReorder
                                blockFirst < 0 || index < blockFirst -> contextBeforeReorder
                                else -> contextAfterReorder
                            }
                        item(key = row.id) {
                            ReorderableItem(
                                state = section,
                                key = row.id,
                                enabled = enabled,
                                animateItemModifier =
                                    Modifier.animateItem(
                                        fadeInSpec = tween(150),
                                        fadeOutSpec = tween(150),
                                        placementSpec = tween(200),
                                    ),
                            ) { isDragging ->
                                QueueRowItem(
                                    row = row,
                                    isPlaying = row.song.song.id == currentSongId,
                                    floating = isDragging,
                                    coverArt = coverArt,
                                    download = downloads[row.song.song.id],
                                    dragHandle =
                                        if (!enabled) {
                                            null
                                        } else {
                                            Modifier.draggableHandle(
                                                onDragStarted = {
                                                    dragId = row.id
                                                    dragFrom = row.position
                                                },
                                                onDragStopped = {
                                                    val id = dragId
                                                    val from = dragFrom
                                                    if (id != null && from != null) {
                                                        val draggedIndex = rows.indexOfFirst { it.id == id }
                                                        val to =
                                                            if (draggedIndex < 0) {
                                                                from
                                                            } else {
                                                                dropTarget(rows, draggedIndex, from)
                                                            }
                                                        if (to != from) {
                                                            onMove(from, to)
                                                            showUndo("Queue reordered")
                                                        }
                                                    }
                                                    dragId = null
                                                    dragFrom = null
                                                },
                                            )
                                        },
                                    onClick = { onPlay(row.position) },
                                    onRemove = {
                                        onRemove(row.position)
                                        showUndo("Removed from queue")
                                    },
                                )
                            }
                        }
                    }
                    item(key = "fill") { Spacer(Modifier.height(fill)) }
                }
            }
        }
    }
}

@Composable
private fun QueueSectionHeader(
    label: String,
    onClear: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (onClear != null) {
            IconButton(onClick = onClear) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear up next", modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun List<QueueRow>.rowIndexOf(key: Any?): Int {
    val id = key as? Long ?: return -1
    return indexOfFirst { it.id == id }
}

private fun List<QueueRow>.lazyIndexOf(rowIndex: Int): Int {
    if (rowIndex !in indices) return 0
    var headers = 0
    for (i in 0..rowIndex) {
        if (i == 0 || this[i - 1].upNext != this[i].upNext) headers++
    }
    return rowIndex + headers
}

internal fun dropTarget(
    rows: List<QueueRow>,
    index: Int,
    from: Long,
): Long {
    val next = rows.getOrNull(index + 1)
    if (next != null) return next.position - if (next.position > from) 1 else 0
    val before = rows.getOrNull(index - 1)?.position ?: return from
    return (before - if (before > from) 1 else 0) + 1
}

@Composable
private fun QueueRowItem(
    row: QueueRow,
    isPlaying: Boolean,
    floating: Boolean,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    download: SongDownload?,
    dragHandle: Modifier?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    SongRow(
        song = row.song.song,
        coverArtId = row.song.coverArt,
        coverArt = coverArt,
        download = download,
        isPlaying = isPlaying,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove from queue",
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (dragHandle != null) {
                    Box(modifier = Modifier.size(40.dp).then(dragHandle), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.DragHandle, contentDescription = "Reorder")
                    }
                }
            }
        },
        modifier =
            Modifier
                .background(
                    if (floating) {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
                    } else {
                        Color.Transparent
                    },
                ).clickable(onClick = onClick),
    )
}
