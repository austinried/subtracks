package com.subtracks.ui.library

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.ui.components.CoverArt

private val TRACK_COLUMN_WIDTH = 24.dp
private val TIME_COLUMN_WIDTH = 36.dp
private val PLAY_ICON_SIZE = 22.dp
private val MIN_SMALL_FONT = 8.sp

@Composable
fun SongRow(
    song: Song,
    coverArtId: String? = null,
    coverArt: ((String?, Boolean) -> CoverArtRef?)? = null,
    isPlaying: Boolean = false,
    trackNumber: Long? = null,
    durationSeconds: Long? = null,
    download: SongDownload? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leadingContent =
            when {
                coverArt != null -> {
                    {
                        CoverArtCell(
                            ref = coverArt(coverArtId, true),
                            name = song.album ?: song.title,
                            isPlaying = isPlaying,
                        )
                    }
                }

                trackNumber != null || isPlaying -> {
                    { TrackCell(trackNumber, isPlaying) }
                }

                else -> {
                    null
                }
            },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (download.showsIndicator()) DownloadBadge(download)
                if (durationSeconds != null) TrackTime(durationSeconds)
                trailingContent?.invoke()
            }
        },
        headlineContent = {
            Text(
                text = song.title,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Unspecified,
                fontWeight = if (isPlaying) FontWeight.SemiBold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        supportingContent = {
            Text(
                text = song.artist.orEmpty().ifEmpty { "\u00A0" },
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

private fun SongDownload?.showsIndicator(): Boolean =
    when (this?.status) {
        DownloadStatus.Completed, DownloadStatus.Queued, DownloadStatus.Running -> true
        else -> false
    }

@Composable
private fun CoverArtCell(
    ref: CoverArtRef?,
    name: String,
    isPlaying: Boolean,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(Modifier.size(48.dp)) {
        CoverArt(
            ref = ref,
            name = name,
            modifier = Modifier.size(48.dp).clip(shape),
        )
        if (isPlaying) {
            Box(
                Modifier
                    .matchParentSize()
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, shape),
            )
            PlayIndicator(
                shadow = true,
                modifier = Modifier.align(Alignment.Center).size(PLAY_ICON_SIZE),
            )
        }
    }
}

@Composable
private fun PlayIndicator(
    shadow: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (shadow) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.5f),
                modifier =
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = 1.35f
                            scaleY = 1.35f
                        }.blur(3.dp, BlurredEdgeTreatment.Unbounded),
            )
        }
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = "Playing",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun TrackCell(
    track: Long?,
    isPlaying: Boolean,
) {
    Box(
        modifier = Modifier.width(TRACK_COLUMN_WIDTH),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlaying) {
            PlayIndicator(shadow = false, modifier = Modifier.size(PLAY_ICON_SIZE))
        } else if (track != null) {
            SmallNumber(track.toString())
        }
    }
}

@Composable
private fun TrackTime(seconds: Long) {
    SmallNumber(
        text = formatTrackTime(seconds),
        modifier = Modifier.width(TIME_COLUMN_WIDTH),
    )
}

@Composable
private fun SmallNumber(
    text: String,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodySmall
    BasicText(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = style.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant),
        maxLines = 1,
        autoSize =
            TextAutoSize.StepBased(
                minFontSize = MIN_SMALL_FONT,
                maxFontSize = style.fontSize,
                stepSize = 1.sp,
            ),
    )
}

internal fun formatTrackTime(seconds: Long): String {
    val total = seconds.coerceAtLeast(0)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val secs = total % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%02d:%02d".format(minutes, secs)
    }
}

@Composable
private fun DownloadBadge(download: SongDownload?) {
    when (download?.status) {
        DownloadStatus.Completed -> {
            Icon(
                imageVector = Icons.Rounded.DownloadDone,
                contentDescription = "Downloaded",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }

        DownloadStatus.Queued, DownloadStatus.Running -> {
            val progress = download.progress
            if (progress == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp).semantics { contentDescription = "Downloading" },
                    strokeWidth = 2.dp,
                )
            } else {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(14.dp).semantics { contentDescription = "Downloading" },
                    strokeWidth = 2.dp,
                )
            }
        }

        else -> {
            Unit
        }
    }
}
