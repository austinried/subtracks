package com.subtracks.ui.playback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.CoverArtRef
import com.subtracks.playback.PlaybackState
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.playerSurfaceColor

@Composable
fun MiniPlayer(
    state: PlaybackState,
    coverArt: CoverArtRef?,
    artwork: ArtworkColors?,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onExpandDrag: (Float) -> Unit = {},
    onExpandRelease: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val item = state.item ?: return
    var dragUpPx by remember { mutableFloatStateOf(0f) }
    val progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f

    ArtworkTheme(artwork) {
        Surface(
            color =
                artwork?.let(::playerSurfaceColor)
                    ?: MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Vertical,
                        onDragStarted = { dragUpPx = 0f },
                        state =
                            rememberDraggableState { delta ->
                                dragUpPx = (dragUpPx - delta).coerceAtLeast(0f)
                                onExpandDrag(dragUpPx)
                            },
                        onDragStopped = { velocity ->
                            onExpandRelease(velocity)
                            dragUpPx = 0f
                        },
                    ).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onExpand,
                    ),
        ) {
            Column {
                MiniPlayerProgressBar(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CoverArt(
                        ref = coverArt,
                        name = item.title,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                        )
                        Text(
                            text = item.artist ?: item.album.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPlayPause, modifier = Modifier.size(48.dp)) {
                            if (state.isBuffering && state.isPlaying) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }
                        IconButton(onClick = onNext, enabled = state.hasNext, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    val track = color.copy(alpha = 0.24f)
    Canvas(modifier) {
        val radius = size.height / 2f
        drawRect(color = track)
        val tip = size.width * progress.coerceIn(0f, 1f)
        if (tip <= 0f) return@Canvas
        drawRoundRect(
            color = color,
            size = Size(tip, size.height),
            cornerRadius = CornerRadius(radius, radius),
        )
        if (tip > radius) {
            drawRect(color = color, size = Size(tip - radius, size.height))
        }
    }
}
