package com.subtracks.ui.playback

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOn
import androidx.compose.material.icons.rounded.RepeatOneOn
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueKind
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.RepeatMode
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
    val positionMs by controller.positionMs.collectAsStateWithLifecycle()
    val context = LocalPlatformContext.current
    val art = controller.coverArt(state.item)
    val thumbnail = controller.coverArt(state.item, thumbnail = true)
    val queueContext = state.context
    val fallbackTitle =
        if (queueContext?.kind == QueueKind.Songs) {
            "Library"
        } else {
            state.item?.album?.takeIf { it.isNotBlank() } ?: "Library"
        }
    val sourceTitle = remember(queueContext, fallbackTitle) { mutableStateOf(fallbackTitle) }
    LaunchedEffect(queueContext, fallbackTitle) {
        sourceTitle.value = fallbackTitle
        sourceTitle.value = controller.sourceTitle(queueContext)?.takeIf { it.isNotBlank() } ?: fallbackTitle
    }
    LaunchedEffect(state.item?.id, state.hasNext) {
        if (!state.hasNext) return@LaunchedEffect
        controller.upcomingCoverArt(thumbnail = true)?.let { ArtworkSeedCache.prefetch(context, it) }
        controller.upcomingCoverArt()?.let { prefetchImage(context, it) }
    }
    NowPlayingScreen(
        state = state,
        positionMs = positionMs,
        title = sourceTitle.value,
        coverArt = art,
        thumbnailRef = thumbnail,
        artwork = rememberArtworkColors(thumbnail ?: art),
        onBack = onBack,
        onQueue = onQueue,
        onPlayPause = controller::togglePlayPause,
        onNext = controller::next,
        onPrevious = controller::previous,
        onShuffle = controller::toggleShuffle,
        onRepeat = controller::cycleRepeat,
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
    positionMs: Long,
    title: String,
    coverArt: CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onQueue: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit = {},
    onRepeat: () -> Unit = {},
    onSeek: (Long) -> Unit,
    thumbnailRef: CoverArtRef? = null,
    modifier: Modifier = Modifier,
) {
    val playButtonSize = 90.dp
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
                                Column {
                                    Text(
                                        text = "Now playing: ${state.context?.kind?.label() ?: "library"}".uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
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
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CoverArt(
                                ref = coverArt,
                                name = state.item?.title.orEmpty(),
                                thumbnailRef = thumbnailRef,
                                square = false,
                                elevation = 3.dp,
                                modifier = Modifier.fillMaxSize(),
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
                                    .padding(top = 12.dp)
                                    .height(titleHeight + 6.dp + subtitleHeight),
                        ) {
                            Box(Modifier.height(titleHeight), contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.item?.title.orEmpty(),
                                    style = titleStyle,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee(),
                                )
                            }
                            Box(Modifier.height(subtitleHeight), contentAlignment = Alignment.Center) {
                                Text(
                                    text = listOfNotNull(state.item?.artist, state.item?.album).joinToString(" • "),
                                    style = subtitleStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee(),
                                )
                            }
                        }
                        var dragging by remember { mutableStateOf(false) }
                        var dragPosition by remember { mutableFloatStateOf(0f) }
                        Slider(
                            value = if (dragging) dragPosition else positionMs.toFloat(),
                            onValueChange = {
                                dragging = true
                                dragPosition = it
                            },
                            onValueChangeFinished = {
                                onSeek(dragPosition.toLong())
                                dragging = false
                            },
                            valueRange = 0f..state.durationMs.toFloat().coerceAtLeast(1f),
                            enabled = state.durationMs > 0,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(formatTime(positionMs), style = MaterialTheme.typography.bodySmall)
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
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onRepeat, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector =
                                        when (state.repeat) {
                                            RepeatMode.Off -> Icons.Rounded.Repeat
                                            RepeatMode.All -> Icons.Rounded.RepeatOn
                                            RepeatMode.One -> Icons.Rounded.RepeatOneOn
                                        },
                                    contentDescription =
                                        when (state.repeat) {
                                            RepeatMode.Off -> "Repeat off"
                                            RepeatMode.All -> "Repeat all"
                                            RepeatMode.One -> "Repeat one"
                                        },
                                    tint =
                                        if (state.repeat == RepeatMode.Off) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        },
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                            IconButton(onClick = onPrevious, modifier = Modifier.size(72.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(60.dp),
                                )
                            }
                            IconButton(onClick = onPlayPause, modifier = Modifier.size(playButtonSize)) {
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
                            IconButton(onClick = onNext, modifier = Modifier.size(72.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(60.dp),
                                )
                            }
                            IconButton(onClick = onShuffle, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = if (state.shuffle) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint =
                                        if (state.shuffle) {
                                            MaterialTheme.colorScheme.onBackground
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            IconButton(onClick = onQueue, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = "Queue",
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = "More",
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

private fun QueueKind.label(): String =
    when (this) {
        QueueKind.Album -> "album"
        QueueKind.Playlist -> "playlist"
        QueueKind.Song -> "song"
        QueueKind.Songs -> "songs"
    }

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
