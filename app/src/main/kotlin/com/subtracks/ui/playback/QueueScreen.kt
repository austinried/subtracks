package com.subtracks.ui.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.repo.QUEUE_PAGE_SIZE
import com.subtracks.data.repo.QueuePagingSource
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueWindowItem
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.library.SongRow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private var source: QueuePagingSource? = null

    val items: Flow<PagingData<QueueWindowItem>> =
        Pager(PagingConfig(pageSize = QUEUE_PAGE_SIZE, enablePlaceholders = false)) {
            QueuePagingSource(queueRepository).also { source = it }
        }.flow.cachedIn(viewModelScope)

    fun play(position: Long) {
        viewModelScope.launch { playbackController.playAt(position) }
    }

    fun remove(position: Long) {
        viewModelScope.launch {
            playbackController.removeAt(position)
            source?.invalidate()
        }
    }

    fun move(
        from: Long,
        to: Long,
    ) {
        if (from == to) return
        viewModelScope.launch {
            playbackController.move(from, to)
            source?.invalidate()
        }
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
    QueueScreen(
        items = viewModel.items.collectAsLazyPagingItems(),
        currentPosition = playback.position,
        coverArt = sourceRepository::coverArt,
        onBack = onBack,
        onPlay = viewModel::play,
        onRemove = viewModel::remove,
        onMove = viewModel::move,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    items: LazyPagingItems<QueueWindowItem>,
    currentPosition: Long?,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onPlay: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onMove: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var rowHeight by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var centered by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition, items.itemCount) {
        val position = currentPosition ?: return@LaunchedEffect
        if (!centered && position.toInt() < items.itemCount) {
            listState.scrollToItem(position.toInt())
            centered = true
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
    ) { padding ->
        when {
            items.itemCount == 0 && items.loadState.refresh is LoadState.Loading -> {
                LoadingState(Modifier.padding(padding))
            }

            items.itemCount == 0 -> {
                EmptyState("The queue is empty.", Modifier.padding(padding))
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(count = items.itemCount) { index ->
                        val item = items[index]
                        if (item != null) {
                            QueueRow(
                                item = item,
                                isPlaying = item.position == currentPosition,
                                coverArt = coverArt,
                                dragging = dragging == item.position,
                                dragOffset = if (dragging == item.position) dragOffset else 0f,
                                onClick = { onPlay(item.position) },
                                onRemove = { onRemove(item.position) },
                                onSize = { rowHeight = it.toFloat() },
                                onDragStart = {
                                    dragging = item.position
                                    dragOffset = 0f
                                },
                                onDrag = { dragOffset += it },
                                onDragEnd = {
                                    val from = dragging
                                    if (from != null && rowHeight > 0f) {
                                        val to =
                                            (from + (dragOffset / rowHeight).roundToInt())
                                                .coerceIn(0L, (items.itemCount - 1).toLong())
                                        if (to != from) onMove(from, to)
                                    }
                                    dragging = null
                                    dragOffset = 0f
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
    coverArt: (String?, Boolean) -> CoverArtRef?,
    dragging: Boolean,
    dragOffset: Float,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onSize: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
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
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .pointerInput(item.position) {
                                detectDragGestures(
                                    onDragStart = { onDragStart() },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        onDrag(amount.y)
                                    },
                                    onDragEnd = onDragEnd,
                                    onDragCancel = onDragEnd,
                                )
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = Icons.Rounded.DragHandle, contentDescription = "Reorder")
                }
            }
        },
        modifier =
            Modifier
                .zIndex(if (dragging) 1f else 0f)
                .graphicsLayer { translationY = dragOffset }
                .onSizeChanged { size -> onSize(size.height) }
                .clickable(onClick = onClick),
    )
}
