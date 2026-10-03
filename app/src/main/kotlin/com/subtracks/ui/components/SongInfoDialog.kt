package com.subtracks.ui.components

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subtracks.R
import com.subtracks.data.media.readAudioEncoding
import com.subtracks.data.model.AudioEncoding
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import java.io.File

@Composable
fun SongInfoDialog(
    song: Song,
    download: SongDownload?,
    localFile: File?,
    streamEncoding: AudioEncoding?,
    onDismiss: () -> Unit,
    genres: List<String>,
) {
    val context = LocalContext.current
    var encoding by remember(song.id, localFile) { mutableStateOf<AudioEncoding?>(null) }
    LaunchedEffect(song.id, localFile) {
        encoding = localFile?.let { readAudioEncoding(it) }
    }
    val shown = if (download?.status == DownloadStatus.Completed) encoding else streamEncoding
    val sizeBytes = localFile?.length()?.takeIf { it > 0 }
    val averageBps = sizeBytes?.let { bytes -> song.duration?.takeIf { it > 0 }?.let { seconds -> (bytes * 8 / seconds).toInt() } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(stringResource(R.string.resources_sort_by_title), song.title)
                InfoRow(stringResource(R.string.resources_filter_artist), song.artist)
                InfoRow(stringResource(R.string.resources_filter_album), song.album)
                InfoRow(stringResource(R.string.info_duration), song.duration?.let(DateUtils::formatElapsedTime))
                InfoRow(
                    stringResource(R.string.info_track),
                    song.track?.let { track ->
                        song.disc?.let { disc -> stringResource(R.string.info_track_number, disc, track) }
                            ?: track.toString()
                    },
                )
                InfoRow(stringResource(R.string.resources_filter_genre), genres.joinToString(", ").ifBlank { null })
                InfoRow(stringResource(R.string.info_starred), if (song.starred != null) stringResource(R.string.info_yes) else null)
                if (download != null) {
                    InfoRow(stringResource(R.string.actions_download), downloadStatus(download.status))
                    InfoRow(stringResource(R.string.info_size), sizeBytes?.let { Formatter.formatFileSize(context, it) })
                    download.error?.let { InfoRow(stringResource(R.string.info_error), stringResource(it.messageRes)) }
                }
                InfoRow(stringResource(R.string.info_format), shown?.format)
                // A decoder often reports no bitrate; the file's own size over the track length does.
                InfoRow(
                    stringResource(R.string.info_bitrate),
                    (shown?.bitrate?.takeIf { it > 0 } ?: averageBps)?.let { stringResource(R.string.bitrate_value, it / 1000) },
                )
                InfoRow(
                    stringResource(R.string.info_sample_rate),
                    shown
                        ?.sampleRate
                        ?.takeIf {
                            it > 0
                        }?.let { stringResource(R.string.info_sample_rate_value, it) },
                )
                InfoRow(stringResource(R.string.info_channels), shown?.channels?.takeIf { it > 0 }?.toString())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.info_close)) } },
    )
}

@Composable
private fun InfoRow(
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

private val INFO_LABEL_WIDTH = 80.dp

@Composable
private fun downloadStatus(status: DownloadStatus): String =
    when (status) {
        DownloadStatus.Completed -> stringResource(R.string.status_downloaded)
        DownloadStatus.Queued -> stringResource(R.string.status_queued)
        DownloadStatus.Running -> stringResource(R.string.status_downloading)
        DownloadStatus.Failed -> stringResource(R.string.status_failed)
    }
