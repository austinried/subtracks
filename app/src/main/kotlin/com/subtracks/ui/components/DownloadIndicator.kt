package com.subtracks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.ListDownloadStatus

const val DOWNLOADED_INDICATOR_TAG = "Downloaded"
const val DOWNLOADING_INDICATOR_TAG = "Downloading"

enum class DownloadIndicator { Hidden, InProgress, Complete }

fun ListDownloadStatus.indicator(): DownloadIndicator =
    when {
        total == 0L || (downloaded == 0L && downloading == 0L) -> DownloadIndicator.Hidden
        complete -> DownloadIndicator.Complete
        else -> DownloadIndicator.InProgress
    }

@Composable
fun ListDownloadIndicator(
    status: ListDownloadStatus?,
    modifier: Modifier = Modifier,
) {
    val current = status ?: return
    val kind = current.indicator()
    if (kind == DownloadIndicator.Hidden) return
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (kind) {
                DownloadIndicator.Complete -> {
                    Icon(
                        imageVector = Icons.Rounded.DownloadDone,
                        contentDescription = DOWNLOADED_INDICATOR_TAG,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                DownloadIndicator.InProgress -> {
                    CircularProgressIndicator(
                        progress = { current.downloaded.toFloat() / current.total },
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp).semantics { contentDescription = DOWNLOADING_INDICATOR_TAG },
                    )
                }

                DownloadIndicator.Hidden -> {
                    Unit
                }
            }
        }
    }
}
