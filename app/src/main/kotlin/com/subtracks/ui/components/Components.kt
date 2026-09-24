package com.subtracks.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
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
    square: Boolean = true,
    elevation: Dp = 0.dp,
) {
    val context = LocalPlatformContext.current
    var failed by remember(ref, thumbnailRef) { mutableStateOf(false) }
    var thumbnailLoaded by remember(ref, thumbnailRef) { mutableStateOf(false) }
    val model = remember(ref, thumbnailRef) { ref?.let { imageRequest(context, it, crossfade = thumbnailRef != null) } }
    val frameModifier =
        Modifier
            .shadow(elevation, RoundedCornerShape(2.dp), clip = elevation > 0.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)

    if (square) {
        Box(modifier.then(frameModifier)) {
            CoverArtContent(
                ref = ref,
                name = name,
                thumbnailRef = thumbnailRef,
                showPlaceholder = showPlaceholder,
                failed = failed,
                thumbnailLoaded = thumbnailLoaded,
                onThumbnailLoaded = { thumbnailLoaded = true },
                contentScale = ContentScale.Crop,
            ) {
                if (ref != null) {
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { failed = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    } else {
        val painter =
            rememberAsyncImagePainter(
                model = model,
                onError = { failed = true },
                contentScale = ContentScale.Fit,
            )
        BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
            val ratio = painter.intrinsicSize.ratioOrOne()
            val width = if (ratio > maxWidth.value / maxHeight.value) maxWidth else maxHeight * ratio
            Box(Modifier.size(width, width / ratio).then(frameModifier)) {
                CoverArtContent(
                    ref = ref,
                    name = name,
                    thumbnailRef = thumbnailRef,
                    showPlaceholder = showPlaceholder,
                    failed = failed,
                    thumbnailLoaded = thumbnailLoaded,
                    onThumbnailLoaded = { thumbnailLoaded = true },
                    contentScale = ContentScale.Fit,
                ) {
                    if (ref != null) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CoverArtContent(
    ref: CoverArtRef?,
    name: String,
    thumbnailRef: CoverArtRef?,
    showPlaceholder: Boolean,
    failed: Boolean,
    thumbnailLoaded: Boolean,
    onThumbnailLoaded: () -> Unit,
    contentScale: ContentScale,
    main: @Composable () -> Unit,
) {
    val context = LocalPlatformContext.current
    if (showPlaceholder || (failed && !thumbnailLoaded)) {
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
            contentScale = contentScale,
            onSuccess = { onThumbnailLoaded() },
            modifier = Modifier.fillMaxSize(),
        )
    }
    main()
}

private fun Size.ratioOrOne(): Float = if (width > 0f && height > 0f && width.isFinite() && height.isFinite()) width / height else 1f

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
