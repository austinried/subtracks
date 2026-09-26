package com.subtracks.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Song
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.StarredBadge

@Composable
fun SongRow(
    song: Song,
    coverArtId: String? = null,
    coverArt: ((String?, Boolean) -> CoverArtRef?)? = null,
    isPlaying: Boolean = false,
    trackNumber: Long? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val leadingTrack = trackNumber?.takeIf { coverArt == null }
    ListItem(
        modifier = modifier,
        leadingContent =
            when {
                coverArt != null -> {
                    {
                        CoverArt(
                            ref = coverArt(coverArtId, true),
                            name = song.album ?: song.title,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                        )
                    }
                }

                leadingTrack != null -> {
                    { TrackNumber(leadingTrack, isPlaying) }
                }

                else -> {
                    null
                }
            },
        trailingContent = trailingContent,
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isPlaying && leadingTrack == null) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Playing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = song.title,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    fontWeight = if (isPlaying) FontWeight.SemiBold else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                StarredBadge(song.starred != null)
            }
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

@Composable
private fun TrackNumber(
    track: Long,
    isPlaying: Boolean,
) {
    Box(
        modifier = Modifier.width(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlaying) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Playing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Text(
                text = track.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
