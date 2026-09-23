package com.subtracks.ui.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.gradientColorAt
import com.subtracks.ui.theme.rememberArtworkColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

private const val DOT = "\u00B7"
private const val GRADIENT_SCREENS = 2.0f
private const val FADE_DISTANCE_DP = 64

@Composable
fun AlbumDetailRoute(
    albumId: String,
    onBack: () -> Unit,
    viewModel: AlbumDetailViewModel = koinViewModel(key = albumId) { parametersOf(albumId) },
    playbackController: PlaybackController = koinInject(),
) {
    val album by viewModel.album.collectAsStateWithLifecycle(initialValue = null)
    val songs by viewModel.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    AlbumDetailScreen(
        album = album,
        songs = songs,
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(viewModel.coverArt(album?.coverArt, true)),
        onBack = onBack,
        onSongClick = viewModel::play,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Album && context.refId == albumId },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: Album?,
    songs: List<Song>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onDownload: () -> Unit = {},
    onShuffle: () -> Unit = {},
    onMore: () -> Unit = {},
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    ArtworkTheme(artwork) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
                val listState = rememberLazyListState()
                val fill = rememberViewportFill(listState)
                val density = LocalDensity.current
                val gradientHeight = maxHeight * GRADIENT_SCREENS
                val gradientHeightPx = with(density) { gradientHeight.toPx() }
                val statusBarTop = WindowInsets.statusBars.getTop(density)
                val navBarBottom = WindowInsets.navigationBars.getBottom(density)
                val statusBarDp = with(density) { statusBarTop.toDp() }
                val barHeightPx = statusBarTop + with(density) { TopAppBarDefaults.TopAppBarExpandedHeight.toPx() }
                val fadeDistancePx = with(density) { FADE_DISTANCE_DP.dp.toPx() }

                var headerHeightPx by remember { mutableFloatStateOf(0f) }
                var rowHeightPx by remember { mutableFloatStateOf(0f) }
                var controlsTopPx by remember { mutableFloatStateOf(0f) }
                val scrollPx by remember {
                    derivedStateOf {
                        val index = listState.firstVisibleItemIndex
                        val before = if (index <= 0) 0f else headerHeightPx + rowHeightPx * (index - 1)
                        (before + listState.firstVisibleItemScrollOffset).coerceAtLeast(0f)
                    }
                }
                val barFraction by remember {
                    derivedStateOf {
                        if (controlsTopPx <= 0f || fadeDistancePx <= 0f) {
                            0f
                        } else {
                            ((scrollPx - (controlsTopPx - barHeightPx)) / fadeDistancePx).coerceIn(0f, 1f)
                        }
                    }
                }
                val gradientAlpha by animateFloatAsState(
                    targetValue = if (artwork != null) 1f else 0f,
                    animationSpec = tween(durationMillis = 600),
                    label = "gradientAlpha",
                )
                val barColor =
                    artwork?.gradientColorAt((scrollPx + barHeightPx / 2f) / gradientHeightPx) ?: Color.Black

                HeroGradient(
                    colors = artwork,
                    scrollPx = { scrollPx },
                    modifier = Modifier.fillMaxSize().alpha(gradientAlpha),
                )

                LazyColumn(
                    state = listState,
                    contentPadding =
                        PaddingValues(
                            bottom = 16.dp + with(density) { navBarBottom.toDp() },
                        ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        AlbumHeader(
                            album = album,
                            hasSongs = songs.isNotEmpty(),
                            coverArt = coverArt,
                            onPlay = { onSongClick(0) },
                            onShuffle = onShuffle,
                            onDownload = onDownload,
                            onMore = onMore,
                            topInset = statusBarDp,
                            controlsModifier =
                                Modifier.onGloballyPositioned {
                                    controlsTopPx = it.positionInParent().y
                                },
                            modifier = Modifier.onSizeChanged { headerHeightPx = it.height.toFloat() },
                        )
                    }
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        SongRow(
                            song = song,
                            isPlaying = song.id == playingSongId,
                            modifier =
                                Modifier
                                    .onSizeChanged { rowHeightPx = it.height.toFloat() }
                                    .clickable { onSongClick(index) },
                        )
                    }
                    item { Spacer(Modifier.height(fill)) }
                }

                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(statusBarDp + 8.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                            ),
                        ).alpha(1f - barFraction),
                )

                TopAppBar(
                    title = {
                        Text(
                            text = album?.name.orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.alpha(barFraction),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.alpha(barFraction)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .background(barColor.copy(alpha = barFraction))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {},
                )
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    album: Album?,
    hasSongs: Boolean,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    onMore: () -> Unit,
    topInset: Dp,
    controlsModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = topInset + 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CoverArt(
            ref = coverArt(album?.coverArt, false),
            name = album?.name.orEmpty(),
            modifier = Modifier.fillMaxWidth(0.86f).aspectRatio(1f).clip(RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = album?.name.orEmpty(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle =
            listOfNotNull(
                album?.albumArtist?.takeIf { it.isNotBlank() },
                album?.year?.takeIf { it > 0 }?.toString(),
            ).joinToString(" $DOT ")
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = controlsModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, contentDescription = "Download")
            }
            Button(onClick = onPlay, enabled = hasSongs) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }
            FilledIconButton(onClick = onShuffle, enabled = hasSongs) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle play")
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Rounded.MoreHoriz, contentDescription = "More options")
            }
        }
    }
}
