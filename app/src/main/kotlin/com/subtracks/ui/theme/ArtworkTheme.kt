package com.subtracks.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.subtracks.data.model.CoverArtRef
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SEED_ART_SIZE_PX = 128

data class ArtworkColors(
    val scheme: ColorScheme,
    val gradientHigh: Color,
    val gradientLow: Color,
    val accents: List<Color>,
)

fun artworkColorsFromSeed(seed: Int): ArtworkColors {
    val (hue, saturation, _) = Color(seed or 0xFF000000.toInt()).toHsl()
    val chroma = saturation.coerceIn(0.30f, 0.72f)
    val secondaryHue = (hue + 60f) % 360f

    fun tone(
        h: Float,
        s: Float,
        l: Float,
    ) = Color.hsl(h % 360f, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))

    val primary = tone(hue, chroma, 0.80f)
    val background = tone(hue, (saturation * 0.7f).coerceIn(0.06f, 0.22f), 0.06f)
    val onBackground = tone(hue, 0.08f, 0.95f)

    val scheme =
        darkColorScheme(
            primary = primary,
            onPrimary = tone(hue, chroma * 0.6f, 0.14f),
            primaryContainer = tone(hue, chroma, 0.32f),
            onPrimaryContainer = tone(hue, chroma * 0.5f, 0.92f),
            secondary = tone(hue, chroma * 0.5f, 0.78f),
            onSecondary = tone(hue, chroma * 0.4f, 0.14f),
            secondaryContainer = tone(hue, chroma * 0.45f, 0.30f),
            onSecondaryContainer = tone(hue, chroma * 0.4f, 0.90f),
            tertiary = tone(secondaryHue, chroma, 0.80f),
            onTertiary = tone(secondaryHue, chroma * 0.6f, 0.14f),
            tertiaryContainer = tone(secondaryHue, chroma, 0.30f),
            onTertiaryContainer = tone(secondaryHue, chroma * 0.5f, 0.90f),
            background = background,
            onBackground = onBackground,
            surface = background,
            onSurface = onBackground,
            surfaceTint = primary,
            surfaceVariant = tone(hue, 0.15f, 0.17f),
            onSurfaceVariant = tone(hue, 0.12f, 0.78f),
            surfaceContainerLowest = tone(hue, 0.16f, 0.04f),
            surfaceContainerLow = tone(hue, 0.16f, 0.08f),
            surfaceContainer = tone(hue, 0.16f, 0.10f),
            surfaceContainerHigh = tone(hue, 0.15f, 0.14f),
            surfaceContainerHighest = tone(hue, 0.14f, 0.18f),
            outline = tone(hue, 0.10f, 0.55f),
            outlineVariant = tone(hue, 0.12f, 0.28f),
        )

    return ArtworkColors(
        scheme = scheme,
        gradientHigh = tone(hue, (saturation * 0.9f).coerceIn(0.30f, 0.60f), 0.24f),
        gradientLow = tone(hue, 0.12f, 0.04f),
        accents = listOf(tone(hue, chroma, 0.60f), tone(secondaryHue, chroma, 0.55f)),
    )
}

@Composable
fun rememberArtworkColors(ref: CoverArtRef?): ArtworkColors? {
    val seed by rememberArtworkSeed(ref)
    return remember(seed) { seed?.let(::artworkColorsFromSeed) }
}

@Composable
private fun rememberArtworkSeed(ref: CoverArtRef?): State<Int?> {
    val context = LocalPlatformContext.current
    return produceState<Int?>(initialValue = null, ref?.cacheKey) {
        value = null
        val art = ref ?: return@produceState
        value =
            withContext(Dispatchers.IO) {
                try {
                    val request =
                        ImageRequest
                            .Builder(context)
                            .data(art.url)
                            .memoryCacheKey(art.cacheKey)
                            .diskCacheKey(art.cacheKey)
                            .size(SEED_ART_SIZE_PX)
                            .allowHardware(false)
                            .build()
                    val image = (SingletonImageLoader.get(context).execute(request) as? SuccessResult)?.image
                    image?.let { seedFrom(it.toBitmap()) }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
    }
}

private fun seedFrom(bitmap: Bitmap): Int? {
    val palette = Palette.from(bitmap).generate()
    return (
        palette.vibrantSwatch
            ?: palette.lightVibrantSwatch
            ?: palette.darkVibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch
    )?.rgb
}

@Composable
fun ArtworkTheme(
    colors: ArtworkColors?,
    content: @Composable () -> Unit,
) {
    val scheme = colors?.scheme
    if (scheme == null) {
        content()
    } else {
        MaterialTheme(colorScheme = scheme, typography = SubtracksTypography, content = content)
    }
}

@Composable
fun HeroGradient(
    colors: ArtworkColors?,
    modifier: Modifier = Modifier,
) {
    val high = colors?.gradientHigh ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val low = colors?.gradientLow ?: MaterialTheme.colorScheme.background
    val accents = colors?.accents.orEmpty()

    Canvas(modifier) {
        drawRect(
            brush =
                Brush.linearGradient(
                    colors = listOf(high, low),
                    start = Offset(size.width * 0.2f, 0f),
                    end = Offset(size.width * 0.8f, size.height),
                ),
        )
        accents.getOrNull(0)?.let { accent ->
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0f)),
                        center = Offset(size.width * 0.15f, size.height * 0.08f),
                        radius = size.width * 0.95f,
                    ),
            )
        }
        accents.getOrNull(1)?.let { accent ->
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.30f), accent.copy(alpha = 0f)),
                        center = Offset(size.width * 0.95f, size.height * 0.35f),
                        radius = size.width * 0.9f,
                    ),
            )
        }
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(high.copy(alpha = 0.45f), high.copy(alpha = 0f)),
                    center = Offset(size.width * 0.55f, size.height * 0.85f),
                    radius = size.width * 1.1f,
                ),
        )
    }
}

private fun Color.toHsl(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val lightness = (max + min) / 2f
    val delta = max - min
    if (delta == 0f) return Triple(0f, 0f, lightness)
    val saturation = if (lightness > 0.5f) delta / (2f - max - min) else delta / (max + min)
    val hue =
        when (max) {
            red -> 60f * (((green - blue) / delta) % 6f)
            green -> 60f * (((blue - red) / delta) + 2f)
            else -> 60f * (((red - green) / delta) + 4f)
        }
    return Triple((hue + 360f) % 360f, saturation, lightness)
}
