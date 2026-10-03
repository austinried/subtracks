package com.subtracks.data.download

import android.content.res.Resources
import com.subtracks.R
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload

data class DownloadNotification(
    val title: String,
    val text: String,
    val progress: Float?,
)

fun downloadNotification(
    resources: Resources,
    rows: Collection<SongDownload>,
): DownloadNotification? {
    val active = rows.filter { it.status == DownloadStatus.Queued || it.status == DownloadStatus.Running }
    if (active.isEmpty()) return null
    val total = active.sumOf { it.total }
    val progress = if (total > 0) (active.sumOf { it.bytes }.toFloat() / total).coerceIn(0f, 1f) else null
    return DownloadNotification(
        title = resources.getQuantityString(R.plurals.download_notification_title, active.size, active.size),
        text =
            progress?.let { resources.getString(R.string.download_notification_progress, (it * 100).toInt()) }
                ?: resources.getString(R.string.download_notification_in_progress),
        progress = progress,
    )
}
