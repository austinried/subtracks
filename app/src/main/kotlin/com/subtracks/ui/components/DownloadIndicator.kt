package com.subtracks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.ListDownloadStatus

const val DOWNLOADED_INDICATOR_TAG = "Downloaded"
const val DOWNLOADING_INDICATOR_TAG = "Downloading"

enum class DownloadIndicator { Hidden, InProgress, Complete }

fun ListDownloadStatus.indicator(): DownloadIndicator =
    when {
        total == 0L -> DownloadIndicator.Hidden
        complete -> DownloadIndicator.Complete
        downloading > 0 -> DownloadIndicator.InProgress
        else -> DownloadIndicator.Hidden
    }

@Composable
fun ListDownloadIndicator(
    status: ListDownloadStatus?,
    modifier: Modifier = Modifier,
) {
    val current = status ?: return
    val kind = current.indicator()
    if (kind == DownloadIndicator.Hidden) return
    Box(modifier = modifier.size(26.dp), contentAlignment = Alignment.Center) {
        when (kind) {
            DownloadIndicator.Complete -> {
                Icon(
                    imageVector = Icons.Rounded.DownloadDone,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp).blur(4.dp).offset(y = 1.dp),
                )
                Icon(
                    imageVector = Icons.Rounded.DownloadDone,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.85f),
                    modifier =
                        Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                scaleX = 1.18f
                                scaleY = 1.18f
                            }.blur(1.dp),
                )
                Icon(
                    imageVector = Icons.Rounded.DownloadDone,
                    contentDescription = DOWNLOADED_INDICATOR_TAG,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            DownloadIndicator.InProgress -> {
                CircularProgressIndicator(
                    progress = { current.downloaded.toFloat() / current.total },
                    color = Color.White,
                    trackColor = Color.Black.copy(alpha = 0.45f),
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(20.dp).semantics { contentDescription = DOWNLOADING_INDICATOR_TAG },
                )
            }

            DownloadIndicator.Hidden -> {
                Unit
            }
        }
    }
}
