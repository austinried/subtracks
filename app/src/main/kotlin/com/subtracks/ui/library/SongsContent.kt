package com.subtracks.ui.library

import android.os.Build
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subtracks.R
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.ui.components.CoverArt

private val TRACK_COLUMN_WIDTH = 24.dp
private val TIME_COLUMN_WIDTH = 36.dp
private val PLAY_ICON_SIZE = 22.dp
private val MIN_SMALL_FONT = 8.sp
private val COVER_CORNER = 6.dp
private val PLAY_SHADOW_RADIUS = 3.dp
private val PLAY_SHADOW = Color.Black.copy(alpha = 0.6f)
private val INNER_SHADOW_RADIUS = 8.dp
private val INNER_SHADOW = Color.Black.copy(alpha = 0.85f)
private val DIM = ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.6f, 0.6f, 0.6f, 1f) })
private val PLAY_ARROW = Icons.Rounded.PlayArrow
private val PLAY_ARROW_PATH: Path by lazy { buildVectorPath(PLAY_ARROW) }

private fun buildVectorPath(vector: ImageVector): Path {
    val parser = PathParser()

    fun collect(group: VectorGroup) {
        for (node in group) {
            when (node) {
                is VectorPath -> parser.addPathNodes(node.pathData)
                is VectorGroup -> collect(node)
            }
        }
    }
    collect(vector.root)
    return parser.toPath()
}

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
    val shape = RoundedCornerShape(COVER_CORNER)
    Box(Modifier.size(48.dp)) {
        CoverArt(
            ref = ref,
            name = name,
            modifier = Modifier.size(48.dp).clip(shape),
            colorFilter = if (isPlaying) DIM else null,
        )
        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Box(Modifier.matchParentSize().drawBehind { drawInnerShadow() })
            }
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

private fun DrawScope.drawInnerShadow() {
    val corner = COVER_CORNER.toPx()
    val shapePath =
        Path().apply {
            addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(corner, corner)))
        }
    val outer =
        Path().apply {
            addRect(Rect(-size.width, -size.height, size.width * 2f, size.height * 2f))
        }
    val ring =
        Path().apply {
            op(outer, shapePath, PathOperation.Difference)
        }
    clipPath(shapePath) {
        drawIntoCanvas { canvas ->
            val paint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.BLACK
                    setShadowLayer(INNER_SHADOW_RADIUS.toPx(), 0f, 0f, INNER_SHADOW.toArgb())
                }
            canvas.nativeCanvas.drawPath(ring.asAndroidPath(), paint)
        }
    }
}

@Composable
private fun PlayIndicator(
    shadow: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = MaterialTheme.colorScheme.primary
    val playingDescription = stringResource(R.string.status_playing)
    if (shadow) {
        val radius = with(LocalDensity.current) { PLAY_SHADOW_RADIUS.toPx() }
        Canvas(modifier.semantics { contentDescription = playingDescription }) {
            val scale = minOf(size.width / PLAY_ARROW.viewportWidth, size.height / PLAY_ARROW.viewportHeight)
            withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
                drawIntoCanvas { canvas ->
                    val path = PLAY_ARROW_PATH.asAndroidPath()
                    val glyph =
                        android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = tint.toArgb()
                        }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        glyph.setShadowLayer(radius, 0f, 0f, PLAY_SHADOW.toArgb())
                        canvas.nativeCanvas.drawPath(path, glyph)
                    } else {
                        val halo =
                            android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = PLAY_SHADOW.toArgb()
                            }
                        canvas.nativeCanvas.save()
                        canvas.nativeCanvas.translate(1f, 1.5f)
                        canvas.nativeCanvas.drawPath(path, halo)
                        canvas.nativeCanvas.restore()
                        canvas.nativeCanvas.drawPath(path, glyph)
                    }
                }
            }
        }
    } else {
        Icon(
            imageVector = PLAY_ARROW,
            contentDescription = playingDescription,
            tint = tint,
            modifier = modifier,
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
        textAlign = TextAlign.End,
    )
}

@Composable
private fun SmallNumber(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    val style = MaterialTheme.typography.bodySmall
    BasicText(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = style.copy(textAlign = textAlign, color = MaterialTheme.colorScheme.onSurfaceVariant),
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
        "%d:%02d".format(minutes, secs)
    }
}

@Composable
private fun DownloadBadge(download: SongDownload?) {
    val downloadedDescription = stringResource(R.string.status_downloaded)
    val downloadingDescription = stringResource(R.string.status_downloading)
    when (download?.status) {
        DownloadStatus.Completed -> {
            Icon(
                imageVector = Icons.Rounded.DownloadDone,
                contentDescription = downloadedDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }

        DownloadStatus.Queued, DownloadStatus.Running -> {
            val progress = download.progress
            if (progress == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp).semantics { contentDescription = downloadingDescription },
                    strokeWidth = 2.dp,
                )
            } else {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(14.dp).semantics { contentDescription = downloadingDescription },
                    strokeWidth = 2.dp,
                )
            }
        }

        else -> {
            Unit
        }
    }
}
