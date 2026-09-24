package com.subtracks.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.subtracks.data.model.CoverArtRef

@Composable
fun CoverArt(
    ref: CoverArtRef?,
    name: String,
    modifier: Modifier = Modifier,
    thumbnailRef: CoverArtRef? = null,
    showPlaceholder: Boolean = true,
) {
    val context = LocalPlatformContext.current
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (showPlaceholder) {
            Text(
                text = name.trim().take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (thumbnailRef != null && thumbnailRef != ref) {
            AsyncImage(
                model = remember(thumbnailRef) { imageRequest(context, thumbnailRef, crossfade = false) },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (ref != null) {
            AsyncImage(
                model = remember(ref, thumbnailRef) { imageRequest(context, ref, crossfade = thumbnailRef != null) },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun imageRequest(
    context: Context,
    ref: CoverArtRef,
    crossfade: Boolean,
): ImageRequest =
    ImageRequest
        .Builder(context)
        .data(ref.url)
        .memoryCacheKey(ref.cacheKey)
        .diskCacheKey(ref.cacheKey)
        .crossfade(crossfade)
        .build()

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
