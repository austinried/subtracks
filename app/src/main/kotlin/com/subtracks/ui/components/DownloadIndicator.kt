package com.subtracks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.subtracks.R
import com.subtracks.data.model.ListDownloadStatus

const val DOWNLOADING_INDICATOR_TAG = "Downloading"

internal fun ListDownloadStatus.isDownloading(): Boolean = total > 0 && downloading > 0

@Composable
fun ListDownloadIndicator(
    status: ListDownloadStatus?,
    modifier: Modifier = Modifier,
    scrim: Boolean = false,
) {
    val current = status ?: return
    if (!current.isDownloading()) return
    val progress = { current.downloaded.toFloat() / current.total }
    if (scrim) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                DownloadRing(progress, shadow = false)
            }
        }
    } else {
        Box(modifier = modifier.size(20.dp), contentAlignment = Alignment.Center) {
            DownloadRing(progress, shadow = true)
        }
    }
}

@Composable
private fun DownloadRing(
    progress: () -> Float,
    shadow: Boolean,
) {
    val downloadingDescription = stringResource(R.string.status_downloading)
    Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        if (shadow) {
            CircularProgressIndicator(
                progress = progress,
                color = Color.Black.copy(alpha = 0.5f),
                trackColor = Color.Transparent,
                strokeWidth = 2.5.dp,
                modifier = Modifier.fillMaxSize().blur(2.dp).offset(y = 1.dp),
            )
        }
        CircularProgressIndicator(
            progress = progress,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeWidth = 2.5.dp,
            modifier = Modifier.fillMaxSize().semantics { contentDescription = downloadingDescription },
        )
    }
}
