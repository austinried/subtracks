package com.subtracks.ui.playback

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
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
import com.subtracks.data.repo.QUEUE_CHUNK
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueWindowItem
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.library.SongRow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val LOAD_THRESHOLD = 5

class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val rows = mutableStateListOf<QueueWindowItem>()

    var ready by mutableStateOf(false)
        private set

    private val mutex = Mutex()
    private var size = 0L
    private var first = 0L
    private var last = -1L

    fun open() {
        viewModelScope.launch {
            mutex.withLock {
                val snapshot = queueRepository.snapshot()
                size = snapshot.size
                rows.clear()
                first = 0
                last = -1
                if (size > 0L) {
                    val cursor = (playbackController.state.value.position ?: 0L).coerceIn(0, size - 1)
                    val start = (cursor - QUEUE_CHUNK / 2).coerceAtLeast(0)
                    val end = (start + QUEUE_CHUNK - 1).coerceAtMost(size - 1)
                    val loaded = queueRepository.range(snapshot, start, end)
                    rows.addAll(loaded)
                    first = loaded.firstOrNull()?.position ?: 0
                    last = loaded.lastOrNull()?.position ?: -1
                }
                ready = true
            }
        }
    }

    fun loadOlder() {
        if (first <= 0L) return
        viewModelScope.launch {
            mutex.withLock {
                if (first <= 0L || rows.isEmpty()) return@withLock
                val snapshot = queueRepository.snapshot()
                size = snapshot.size
                val from = (first - QUEUE_CHUNK).coerceAtLeast(0)
                val loaded = queueRepository.range(snapshot, from, first - 1)
                if (loaded.isEmpty()) return@withLock
                rows.addAll(0, loaded)
                first = loaded.first().position
            }
        }
    }

    fun loadNewer() {
        if (rows.isEmpty() || last >= size - 1) return
        viewModelScope.launch {
            mutex.withLock {
                if (rows.isEmpty() || last >= size - 1) return@withLock
                val snapshot = queueRepository.snapshot()
                size = snapshot.size
                if (last >= size - 1) return@withLock
                val to = (last + QUEUE_CHUNK).coerceAtMost(size - 1)
                val loaded = queueRepository.range(snapshot, last + 1, to)
                if (loaded.isEmpty()) return@withLock
                rows.addAll(loaded)
                last = loaded.last().position
            }
        }
    }

    fun reorder(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex == toIndex) return
        if (fromIndex !in rows.indices || toIndex !in rows.indices) return
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
            mutex.withLock { refresh() }
        }
    }

    fun remove(position: Long) {
        viewModelScope.launch {
            playbackController.removeAt(position)
            mutex.withLock { refresh() }
        }
    }

    fun undo() {
        viewModelScope.launch {
            playbackController.undo()
            mutex.withLock { refresh() }
        }
    }

    private suspend fun refresh() {
        val snapshot = queueRepository.snapshot()
        size = snapshot.size
        if (size == 0L) {
            rows.clear()
            first = 0
            last = -1
            return
        }
        val start = first.coerceIn(0, size - 1)
        val end = (start + rows.size.coerceAtLeast(QUEUE_CHUNK) - 1).coerceAtMost(size - 1)
        val loaded = queueRepository.range(snapshot, start, end)
        rows.clear()
        rows.addAll(loaded)
        first = loaded.firstOrNull()?.position ?: start
        last = loaded.lastOrNull()?.position ?: -1
    }
}

@Composable
fun QueueRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QueueViewModel = koinViewModel(),
    controller: PlaybackController = koinInject(),
    sourceRepository: SourceRepository = koinInject(),
) {
    val playback by controller.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.open() }
    QueueScreen(
        rows = viewModel.rows,
        ready = viewModel.ready,
        currentPosition = playback.position,
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
    rows: List<QueueWindowItem>,
    ready: Boolean,
    currentPosition: Long?,
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
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var dragFrom by remember { mutableStateOf<Long?>(null) }
    var dragTo by remember { mutableStateOf<Long?>(null) }

    val reorderState =
        rememberReorderableLazyListState(listState) { from, to ->
            onReorder(from.index, to.index)
            dragFrom = from.key as? Long
            dragTo = to.key as? Long
        }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val firstVisible = info.visibleItemsInfo.firstOrNull()?.index ?: -1
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            firstVisible to lastVisible
        }.collect { (firstVisible, lastVisible) ->
            if (firstVisible in 0..LOAD_THRESHOLD) onLoadOlder()
            if (lastVisible >= 0 && lastVisible >= rows.size - 1 - LOAD_THRESHOLD) onLoadNewer()
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
                    items(count = rows.size, key = { rows[it].position }) { index ->
                        val row = rows[index]
                        ReorderableItem(
                            state = reorderState,
                            key = row.position,
                            animateItemModifier =
                                Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                    placementSpec = tween(200),
                                ),
                        ) { isDragging ->
                            QueueRow(
                                item = row,
                                isPlaying = row.position == currentPosition,
                                floating = isDragging,
                                coverArt = coverArt,
                                dragHandle =
                                    Modifier.draggableHandle(
                                        onDragStarted = {
                                            dragFrom = row.position
                                            dragTo = row.position
                                        },
                                        onDragStopped = {
                                            val from = dragFrom
                                            val to = dragTo
                                            if (from != null && to != null && from != to) {
                                                onMove(from, to)
                                                showUndo("Queue reordered")
                                            }
                                        },
                                    ),
                                onClick = { onPlay(row.position) },
                                onRemove = {
                                    onRemove(row.position)
                                    showUndo("Removed from queue")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: QueueWindowItem,
    isPlaying: Boolean,
    floating: Boolean,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    dragHandle: Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    SongRow(
        song = item.item.song,
        coverArtId = item.item.coverArt,
        coverArt = coverArt,
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
                Box(modifier = Modifier.size(40.dp).then(dragHandle), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.DragHandle, contentDescription = "Reorder")
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
