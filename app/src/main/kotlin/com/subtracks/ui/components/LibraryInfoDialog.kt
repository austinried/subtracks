package com.subtracks.ui.components

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subtracks.R
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist

private const val ARTIST_SEPARATOR = " \u00B7 "

@Composable
fun AlbumInfoDialog(
    album: Album,
    download: ListDownloadStatus?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(stringResource(R.string.resources_sort_by_title), album.name)
                InfoRow(stringResource(R.string.info_sort_title), album.sortName.takeIfDifferentFrom(album.name))
                InfoRow(stringResource(R.string.info_album_artists), album.artistRefs.names())
                InfoRow(stringResource(R.string.info_album_artist_sort), album.albumArtistSort.takeIfDifferentFrom(album.albumArtist))
                InfoRow(stringResource(R.string.resources_sort_by_year), album.year?.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.resources_filter_genre), album.genre)
                InfoRow(stringResource(R.string.info_songs), album.songCount.takeIf { it > 0 }?.toString())
                download?.takeIf { it.total > 0 }?.let { status ->
                    InfoRow(
                        stringResource(R.string.status_downloaded),
                        stringResource(R.string.info_downloaded_count, status.downloaded, status.total),
                    )
                }
                InfoRow(stringResource(R.string.info_added), album.created.takeIf { it > 0 }?.let { formatDate(context, it) })
                InfoRow(stringResource(R.string.info_last_played), album.played?.let { formatDate(context, it) })
                InfoRow(stringResource(R.string.info_plays), album.playCount.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.info_starred), if (album.starred != null) stringResource(R.string.info_yes) else null)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.info_close)) } },
    )
}

@Composable
fun ArtistInfoDialog(
    artist: Artist,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(stringResource(R.string.resources_filter_artist), artist.name)
                InfoRow(stringResource(R.string.info_sort_name), artist.sortName.takeIfDifferentFrom(artist.name))
                InfoRow(stringResource(R.string.info_albums), artist.albumCount.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.info_songs), artist.songCount.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.info_plays), artist.playCount.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.info_last_played), artist.played?.let { formatDate(context, it) })
                InfoRow(stringResource(R.string.info_starred), if (artist.starred != null) stringResource(R.string.info_yes) else null)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.info_close)) } },
    )
}

@Composable
fun PlaylistInfoDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(stringResource(R.string.resources_sort_by_title), playlist.name)
                InfoRow(stringResource(R.string.info_owner), playlist.owner)
                InfoRow(stringResource(R.string.info_shared), if (playlist.shared) stringResource(R.string.info_yes) else null)
                InfoRow(stringResource(R.string.info_songs), playlist.songCount.takeIf { it > 0 }?.toString())
                InfoRow(stringResource(R.string.info_length), playlist.duration.takeIf { it > 0 }?.let(DateUtils::formatElapsedTime))
                InfoRow(stringResource(R.string.info_created), playlist.created.takeIf { it > 0 }?.let { formatDate(context, it) })
                InfoRow(stringResource(R.string.info_updated), playlist.changed.takeIf { it > 0 }?.let { formatDate(context, it) })
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.info_close)) } },
    )
}

internal fun String?.takeIfDifferentFrom(regular: String?): String? =
    this?.trim()?.takeIf { value -> value.isNotEmpty() && !value.equals(regular?.trim(), ignoreCase = true) }

@Composable
internal fun InfoRow(
    label: String,
    value: String?,
) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(INFO_LABEL_WIDTH),
        )
        Spacer(Modifier.width(INFO_LABEL_GAP))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun List<ArtistRef>.names(): String? =
    map { it.name }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(ARTIST_SEPARATOR)
        .takeIf { it.isNotEmpty() }

private fun formatDate(
    context: Context,
    epochSeconds: Long,
): String =
    DateUtils.formatDateTime(
        context,
        epochSeconds * 1000,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR,
    )

private val INFO_LABEL_WIDTH = 100.dp
private val INFO_LABEL_GAP = 12.dp
