package com.subtracks.data.download

import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload

data class DownloadNotification(
    val title: String,
    val text: String,
    val progress: Float?,
)

fun downloadNotification(rows: Collection<SongDownload>): DownloadNotification? {
    val active = rows.filter { it.status == DownloadStatus.Queued || it.status == DownloadStatus.Running }
    if (active.isEmpty()) return null
    val total = active.sumOf { it.total }
    val progress = if (total > 0) (active.sumOf { it.bytes }.toFloat() / total).coerceIn(0f, 1f) else null
    return DownloadNotification(
        title = if (active.size == 1) "Downloading 1 song" else "Downloading ${active.size} songs",
        text = progress?.let { "${(it * 100).toInt()}%" } ?: "In progress",
        progress = progress,
    )
}
