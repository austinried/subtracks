package com.subtracks.ui.playback

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.subtracks.data.model.CoverArtRef
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.PlaybackState
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkSeedCache
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.rememberArtworkColors
import org.koin.compose.koinInject

@Composable
fun NowPlayingRoute(
    onBack: () -> Unit,
    onQueue: () -> Unit,
    modifier: Modifier = Modifier,
    controller: PlaybackController = koinInject(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val context = LocalPlatformContext.current
    val art = controller.coverArt(state.item)
    val thumbnail = controller.coverArt(state.item, thumbnail = true)
    LaunchedEffect(state.item?.id, state.hasNext) {
        if (!state.hasNext) return@LaunchedEffect
        controller.upcomingCoverArt(thumbnail = true)?.let { ArtworkSeedCache.prefetch(context, it) }
        controller.upcomingCoverArt()?.let { prefetchImage(context, it) }
    }
    NowPlayingScreen(
        state = state,
        coverArt = art,
        thumbnailRef = thumbnail,
        artwork = rememberArtworkColors(thumbnail ?: art),
        onBack = onBack,
        onQueue = onQueue,
        onPlayPause = controller::togglePlayPause,
        onNext = controller::next,
        onPrevious = controller::previous,
        onSeek = controller::seekTo,
        modifier = modifier,
    )
}

private fun prefetchImage(
    context: Context,
    ref: CoverArtRef,
) {
    SingletonImageLoader.get(context).enqueue(
        ImageRequest
            .Builder(context)
            .data(ref.url)
            .memoryCacheKey(ref.cacheKey)
            .diskCacheKey(ref.cacheKey)
            .build(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlaybackState,
    coverArt: CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onQueue: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    thumbnailRef: CoverArtRef? = null,
    modifier: Modifier = Modifier,
) {
    val playButtonSize = 96.dp
    val playCircleDiameter = playButtonSize * 20f / 24f
    val titleStyle =
        MaterialTheme.typography.headlineSmall.copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        )
    val subtitleStyle =
        MaterialTheme.typography.bodyMedium.copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        )

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        ArtworkTheme(artwork) {
            Box(modifier.fillMaxSize().background(Color.Black)) {
                HeroGradient(
                    colors = artwork,
                    scrollPx = { 0f },
                    modifier = Modifier.fillMaxSize(),
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Now playing",
                                    style = MaterialTheme.typography.headlineMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        )
                    },
                ) { padding ->
                    Column(
                        modifier =
                            Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CoverArt(
                                ref = coverArt,
                                name = state.item?.title.orEmpty(),
                                thumbnailRef = thumbnailRef,
                                square = false,
                                elevation = 3.dp,
                                modifier = Modifier.fillMaxHeight(),
                            )
                        }
                        val density = LocalDensity.current
                        val titleHeight =
                            with(density) {
                                MaterialTheme.typography.headlineSmall.lineHeight
                                    .toDp()
                            }
                        val subtitleHeight =
                            with(density) {
                                MaterialTheme.typography.bodyMedium.lineHeight
                                    .toDp()
                            }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier =
                                Modifier
                                    .padding(top = 28.dp)
                                    .height(titleHeight + 6.dp + subtitleHeight),
                        ) {
                            Text(
                                text = state.item?.title.orEmpty(),
                                style = titleStyle,
                                maxLines = 1,
                                modifier = Modifier.height(titleHeight).basicMarquee(),
                            )
                            Text(
                                text = listOfNotNull(state.item?.artist, state.item?.album).joinToString(" • "),
                                style = subtitleStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.height(subtitleHeight).basicMarquee(),
                            )
                        }
                        SeekBar(
                            progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f,
                            onSeek = { fraction -> onSeek((fraction * state.durationMs).toLong()) },
                            modifier = Modifier.padding(top = 28.dp),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(formatTime(state.positionMs), style = MaterialTheme.typography.bodySmall)
                            Text(formatTime(state.durationMs), style = MaterialTheme.typography.bodySmall)
                        }
                        if (state.error != null) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onPrevious, enabled = state.hasPrevious, modifier = Modifier.size(64.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(48.dp),
                                )
                            }
                            IconButton(onClick = onPlayPause, modifier = Modifier.padding(horizontal = 20.dp).size(playButtonSize)) {
                                if (state.isBuffering && state.isPlaying) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(playCircleDiameter)
                                                .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.background,
                                            strokeWidth = 4.dp,
                                            modifier = Modifier.size(playCircleDiameter * 0.65f),
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (state.isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(playButtonSize),
                                    )
                                }
                            }
                            IconButton(onClick = onNext, enabled = state.hasNext, modifier = Modifier.size(64.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(48.dp),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            IconButton(onClick = onQueue, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = "Queue",
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var widthPx by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val active = MaterialTheme.colorScheme.primary
    val track = active.copy(alpha = 0.24f)
    val shown = if (dragging) dragFraction else progress
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(32.dp)
                .onSizeChanged { widthPx = it.width }
                .pointerInput(widthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragging = true
                            dragFraction = if (widthPx > 0) (offset.x / widthPx).coerceIn(0f, 1f) else 0f
                        },
                        onDragEnd = {
                            dragging = false
                            onSeek(dragFraction)
                        },
                        onDragCancel = { dragging = false },
                    ) { change, _ ->
                        dragFraction = if (widthPx > 0) (change.position.x / widthPx).coerceIn(0f, 1f) else 0f
                    }
                }.pointerInput(widthPx) {
                    detectTapGestures { offset ->
                        if (widthPx > 0) onSeek((offset.x / widthPx).coerceIn(0f, 1f))
                    }
                },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(track))
        Box(Modifier.fillMaxWidth(shown).height(4.dp).background(active))
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
