package com.subtracks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.ListDownloadStatus

const val DOWNLOADING_INDICATOR_TAG = "Downloading"

enum class DownloadIndicator { Hidden, InProgress }

fun ListDownloadStatus.indicator(): DownloadIndicator =
    if (total > 0 && downloading > 0) DownloadIndicator.InProgress else DownloadIndicator.Hidden

@Composable
fun ListDownloadIndicator(
    status: ListDownloadStatus?,
    modifier: Modifier = Modifier,
) {
    val current = status ?: return
    if (current.indicator() == DownloadIndicator.Hidden) return
    val progress = { current.downloaded.toFloat() / current.total }
    Box(modifier = modifier.size(20.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = progress,
            color = Color.Black.copy(alpha = 0.5f),
            trackColor = Color.Transparent,
            strokeWidth = 2.5.dp,
            modifier = Modifier.fillMaxSize().blur(2.dp).offset(y = 1.dp),
        )
        CircularProgressIndicator(
            progress = progress,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeWidth = 2.5.dp,
            modifier = Modifier.fillMaxSize().semantics { contentDescription = DOWNLOADING_INDICATOR_TAG },
        )
    }
}
