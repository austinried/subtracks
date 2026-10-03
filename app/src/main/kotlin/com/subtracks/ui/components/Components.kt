package com.subtracks.ui.components

import android.content.Context
import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.size.Dimension
import com.subtracks.data.model.CoverArtRef
import com.subtracks.ui.theme.artworkColorsFromSeed
import com.subtracks.ui.theme.placeholderSeed
import kotlinx.coroutines.CancellationException
import java.lang.Character.UnicodeScript

private const val MAX_CACHED_RATIOS = 256
private const val COVER_ART_FADE_MILLIS = 100
private const val COVER_RATIO_PREFETCH_PX = 512
private const val IDEOGRAPHIC_INITIAL_LIFT = 0.05f

private object ArtworkRatioCache {
    private val ratios = LruCache<String, Float>(MAX_CACHED_RATIOS)

    fun get(cacheKey: String): Float? = ratios.get(cacheKey)

    fun put(
        cacheKey: String,
        ratio: Float,
    ) {
        if (ratio > 0f && ratio.isFinite()) ratios.put(cacheKey, ratio)
    }
}

@Composable
fun CoverArt(
    ref: CoverArtRef?,
    name: String,
    modifier: Modifier = Modifier,
    thumbnailRef: CoverArtRef? = null,
    showPlaceholder: Boolean = true,
    square: Boolean = true,
    elevation: Dp = 0.dp,
    colorFilter: ColorFilter? = null,
) {
    val context = LocalPlatformContext.current
    var failed by remember(ref, thumbnailRef) { mutableStateOf(false) }
    var thumbnailLoaded by remember(ref, thumbnailRef) { mutableStateOf(false) }
    var thumbnailRatio by remember(ref, thumbnailRef) { mutableStateOf(thumbnailRef?.cacheKey?.let(ArtworkRatioCache::get)) }
    val letterScheme =
        if (showPlaceholder || (failed && !thumbnailLoaded)) {
            remember(name) { artworkColorsFromSeed(placeholderSeed(name)).scheme }
        } else {
            null
        }
    val frameModifier =
        Modifier
            .shadow(elevation, RoundedCornerShape(2.dp), clip = elevation > 0.dp)
            .background(letterScheme?.primaryContainer ?: MaterialTheme.colorScheme.surfaceVariant)

    if (square) {
        val model = remember(ref, thumbnailRef) { ref?.let { imageRequest(context, it, crossfade = true) } }
        Box(modifier.then(frameModifier)) {
            CoverArtContent(
                ref = ref,
                name = name,
                thumbnailRef = thumbnailRef,
                showPlaceholder = showPlaceholder,
                failed = failed,
                thumbnailLoaded = thumbnailLoaded,
                onThumbnailLoaded = { thumbnailLoaded = true },
                onThumbnailRatio = { thumbnailRatio = it },
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter,
                placeholderContentColor = letterScheme?.onPrimaryContainer,
            ) {
                if (ref != null) {
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High,
                        colorFilter = colorFilter,
                        onSuccess = { success ->
                            success.painter.intrinsicSize
                                .ratioOrNull()
                                ?.let { ArtworkRatioCache.put(ref.cacheKey, it) }
                        },
                        onError = { failed = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    } else {
        BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            val target =
                remember(maxWidth, maxHeight, density) {
                    val width = with(density) { if (maxWidth.value.isFinite()) maxWidth.roundToPx() else 0 }.takeIf { it > 0 }
                    val height = with(density) { if (maxHeight.value.isFinite()) maxHeight.roundToPx() else 0 }.takeIf { it > 0 }
                    width to height
                }
            val painter =
                rememberAsyncImagePainter(
                    model =
                        remember(ref, thumbnailRef, target) {
                            ref?.let {
                                imageRequest(
                                    context,
                                    it,
                                    crossfade = thumbnailRef != null,
                                    width = target.first,
                                    height = target.second,
                                )
                            }
                        },
                    onError = { failed = true },
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.High,
                )
            val ratio =
                painter.intrinsicSize.ratioOrNull()
                    ?: thumbnailRatio
                    ?: 1f
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
                    onThumbnailRatio = { thumbnailRatio = it },
                    contentScale = ContentScale.Fit,
                    colorFilter = colorFilter,
                    placeholderContentColor = letterScheme?.onPrimaryContainer,
                ) {
                    if (ref != null) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            colorFilter = colorFilter,
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
    onThumbnailRatio: (Float) -> Unit,
    contentScale: ContentScale,
    colorFilter: ColorFilter?,
    placeholderContentColor: Color?,
    main: @Composable () -> Unit,
) {
    val context = LocalPlatformContext.current
    if (showPlaceholder || (failed && !thumbnailLoaded)) {
        val trimmed = name.trim()
        val letter = if (trimmed.isEmpty()) "" else trimmed.substring(0, trimmed.offsetByCodePoints(0, 1)).uppercase()
        val lift =
            with(LocalDensity.current) {
                (MaterialTheme.typography.headlineMedium.fontSize * IDEOGRAPHIC_INITIAL_LIFT).toDp()
            }
        Text(
            text = letter,
            style = MaterialTheme.typography.headlineMedium,
            color = placeholderContentColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = if (letter.isIdeographicInitial()) -lift else 0.dp),
        )
    }
    if (thumbnailRef != null && thumbnailRef != ref) {
        AsyncImage(
            model = remember(thumbnailRef) { imageRequest(context, thumbnailRef, crossfade = false) },
            contentDescription = null,
            contentScale = contentScale,
            colorFilter = colorFilter,
            onSuccess = { success ->
                onThumbnailLoaded()
                success.painter.intrinsicSize
                    .ratioOrNull()
                    ?.let {
                        onThumbnailRatio(it)
                        ArtworkRatioCache.put(thumbnailRef.cacheKey, it)
                    }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
    main()
}

private fun Size.ratioOrNull(): Float? = if (width > 0f && height > 0f && width.isFinite() && height.isFinite()) width / height else null

internal suspend fun prefetchArtworkRatio(
    context: Context,
    ref: CoverArtRef,
) {
    if (ArtworkRatioCache.get(ref.cacheKey) != null) return
    val request =
        imageRequest(
            context,
            ref,
            crossfade = false,
            width = COVER_RATIO_PREFETCH_PX,
            height = COVER_RATIO_PREFETCH_PX,
        )
    val image =
        try {
            (SingletonImageLoader.get(context).execute(request) as? SuccessResult)?.image
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        } ?: return
    if (image.width > 0 && image.height > 0) {
        ArtworkRatioCache.put(ref.cacheKey, image.width.toFloat() / image.height)
    }
}

internal fun String.isIdeographicInitial(): Boolean {
    if (isEmpty()) return false
    return when (UnicodeScript.of(codePointAt(0))) {
        UnicodeScript.HAN, UnicodeScript.HIRAGANA, UnicodeScript.KATAKANA, UnicodeScript.HANGUL -> true
        else -> false
    }
}

internal fun imageRequest(
    context: Context,
    ref: CoverArtRef,
    crossfade: Boolean,
    width: Int? = null,
    height: Int? = null,
): ImageRequest {
    val builder =
        ImageRequest
            .Builder(context)
            .data(ref.url)
            .diskCacheKey(ref.cacheKey)
            .crossfade(if (crossfade) COVER_ART_FADE_MILLIS else 0)
    if (width == null && height == null) {
        builder.memoryCacheKey(ref.cacheKey)
    } else {
        builder
            .size(
                width?.let { Dimension.Pixels(it) } ?: Dimension.Undefined,
                height?.let { Dimension.Pixels(it) } ?: Dimension.Undefined,
            ).memoryCacheKey("${ref.cacheKey}:${width ?: "u"}x${height ?: "u"}")
    }
    return builder.build()
}

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
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun FilteredEmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Filters are hiding everything.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onClearFilters) {
                Text("Clear filters")
            }
        }
    }
}

@Composable
fun CloseWhenDownloadsGone(
    downloaded: Long,
    offline: Boolean,
    onClose: () -> Unit,
) {
    var hadDownloads by remember { mutableStateOf(false) }
    LaunchedEffect(downloaded, offline) {
        if (downloaded > 0) {
            hadDownloads = true
        } else if (hadDownloads && offline) {
            onClose()
        }
    }
}

@Composable
fun Modifier.statusBarScrim(): Modifier {
    val top = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    return this
        .fillMaxWidth()
        .height(top + 8.dp)
        .background(
            Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
            ),
        )
}
