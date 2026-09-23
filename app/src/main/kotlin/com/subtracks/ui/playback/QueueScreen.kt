package com.subtracks.ui.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private var source: QueuePagingSource? = null

    val items: Flow<PagingData<QueueWindowItem>> =
        Pager(PagingConfig(pageSize = QUEUE_PAGE_SIZE, enablePlaceholders = false)) {
            QueuePagingSource(queueRepository, playbackController.state.value.position ?: 0L).also { source = it }
        }.flow

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

    fun undo() {
        viewModelScope.launch {
            playbackController.undo()
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
        onUndo = viewModel::undo,
        modifier = modifier,
    )
}

private enum class DropIndicator { Above, Below }

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
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var rowHeight by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf<Long?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var target by remember { mutableStateOf<Long?>(null) }
    var centered by remember { mutableStateOf(false) }

    val firstPosition = if (items.itemCount > 0) items.peek(0)?.position else null
    LaunchedEffect(currentPosition, items.itemCount, firstPosition) {
        val position = currentPosition ?: return@LaunchedEffect
        val first = firstPosition ?: return@LaunchedEffect
        val index = position - first
        if (!centered && index >= 0 && index < items.itemCount) {
            listState.scrollToItem(index.toInt())
            centered = true
        }
    }

    LaunchedEffect(dragging) {
        if (dragging == null) return@LaunchedEffect
        val threshold = with(density) { 72.dp.toPx() }
        val step = with(density) { 10.dp.toPx() }
        while (true) {
            val from = dragging ?: break
            val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { items.peek(it.index)?.position == from }
            if (info != null) {
                val top = info.offset + dragOffset
                val edge =
                    when {
                        top < listState.layoutInfo.viewportStartOffset + threshold -> -step
                        top + info.size > listState.layoutInfo.viewportEndOffset - threshold -> step
                        else -> 0f
                    }
                if (edge != 0f) {
                    val consumed = listState.scrollBy(edge)
                    dragOffset += consumed
                    target = dropTarget(items, from, dragOffset, rowHeight)
                }
            }
            delay(16)
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
                                indicator = indicatorFor(item.position, dragging, target),
                                onClick = { onPlay(item.position) },
                                onRemove = {
                                    onRemove(item.position)
                                    showUndo("Removed from queue")
                                },
                                onSize = { rowHeight = it.toFloat() },
                                onDragStart = {
                                    dragging = item.position
                                    dragOffset = 0f
                                    target = item.position
                                },
                                onDrag = { amount ->
                                    dragOffset += amount
                                    target = dropTarget(items, item.position, dragOffset, rowHeight)
                                },
                                onDragEnd = {
                                    val from = dragging
                                    val to = target
                                    if (from != null && to != null && to != from) {
                                        onMove(from, to)
                                        showUndo("Queue reordered")
                                    }
                                    dragging = null
                                    dragOffset = 0f
                                    target = null
                                },
                                onDragCancel = {
                                    dragging = null
                                    dragOffset = 0f
                                    target = null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dropTarget(
    items: LazyPagingItems<QueueWindowItem>,
    from: Long,
    offset: Float,
    rowHeight: Float,
): Long {
    if (items.itemCount == 0 || rowHeight <= 0f) return from
    val first = items.peek(0)?.position ?: from
    val last = items.peek(items.itemCount - 1)?.position ?: from
    return (from + (offset / rowHeight).roundToInt()).coerceIn(minOf(first, last), maxOf(first, last))
}

private fun indicatorFor(
    position: Long,
    dragging: Long?,
    target: Long?,
): DropIndicator? =
    when {
        dragging == null || target == null || position != target -> null
        target > dragging -> DropIndicator.Below
        target < dragging -> DropIndicator.Above
        else -> null
    }

@Composable
private fun QueueRow(
    item: QueueWindowItem,
    isPlaying: Boolean,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    dragging: Boolean,
    dragOffset: Float,
    indicator: DropIndicator?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onSize: (Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .zIndex(if (dragging) 1f else 0f)
                .graphicsLayer { translationY = dragOffset },
    ) {
        if (indicator == DropIndicator.Above) DropIndicatorLine()
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
                                        onDragCancel = onDragCancel,
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
                    .background(
                        if (dragging) {
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
                        } else {
                            Color.Transparent
                        },
                    ).onSizeChanged { size -> onSize(size.height) }
                    .clickable(onClick = onClick),
        )
        if (indicator == DropIndicator.Below) DropIndicatorLine()
    }
}

@Composable
private fun DropIndicatorLine() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary),
    )
}
