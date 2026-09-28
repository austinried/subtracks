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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
) {
    val context = LocalContext.current
    var encoding by remember(song.id, localFile) { mutableStateOf<AudioEncoding?>(null) }
    LaunchedEffect(song.id, localFile) {
        encoding = localFile?.let { readAudioEncoding(it) }
    }
    val shown = if (download?.status == DownloadStatus.Completed) encoding else streamEncoding
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("Title", song.title)
                InfoRow("Artist", song.artist)
                InfoRow("Album", song.album)
                InfoRow("Duration", song.duration?.let(DateUtils::formatElapsedTime))
                InfoRow("Track", song.track?.let { track -> song.disc?.let { "$it.$track" } ?: track.toString() })
                InfoRow("Genre", song.genre)
                InfoRow("Starred", if (song.starred != null) "Yes" else null)
                if (download != null) {
                    InfoRow("Download", downloadStatus(download.status))
                    InfoRow("Size", localFile?.length()?.takeIf { it > 0 }?.let { Formatter.formatFileSize(context, it) })
                    download.error?.let { InfoRow("Error", it) }
                }
                shown?.let { enc ->
                    InfoRow("Format", enc.format)
                    InfoRow("Bitrate", enc.bitrate?.takeIf { it > 0 }?.let { "${it / 1000} kbps" })
                    InfoRow("Sample rate", enc.sampleRate?.takeIf { it > 0 }?.let { "$it Hz" })
                    InfoRow("Channels", enc.channels?.takeIf { it > 0 }?.toString())
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String?,
) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
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

private fun downloadStatus(status: DownloadStatus): String =
    when (status) {
        DownloadStatus.Completed -> "Downloaded"
        DownloadStatus.Queued -> "Queued"
        DownloadStatus.Running -> "Downloading"
        DownloadStatus.Failed -> "Failed"
    }
