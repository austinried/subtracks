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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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
import kotlin.math.abs
import kotlin.random.Random

private const val SEED_ART_SIZE_PX = 128
private const val GRADIENT_FADE_START = 1.3f / 2.0f
private const val BLOB_ZONE = 0.62f

data class ArtworkColors(
    val scheme: ColorScheme,
    val gradientHigh: Color,
    val gradientLow: Color,
    val accents: List<Color>,
    val blobSeed: Int,
)

fun artworkColorsFromSeed(seed: Int): ArtworkColors = artworkColorsFromSeeds(seed, null)

fun artworkColorsFromSeeds(
    primarySeed: Int,
    secondarySeed: Int?,
): ArtworkColors {
    val (hue, saturation, _) = Color(primarySeed or 0xFF000000.toInt()).toHsl()
    val s = saturation.coerceAtMost(0.85f)

    fun tone(
        h: Float,
        sat: Float,
        l: Float,
    ) = Color.hsl(h % 360f, sat.coerceIn(0f, 1f), l.coerceIn(0f, 1f))

    val primary = tone(hue, s, 0.62f)
    val background = tone(hue, (s * 0.35f).coerceAtMost(0.20f), 0.06f)
    val onBackground = tone(hue, (s * 0.10f).coerceAtMost(0.08f), 0.95f)

    val scheme =
        darkColorScheme(
            primary = primary,
            onPrimary = tone(hue, s * 0.6f, 0.12f),
            primaryContainer = tone(hue, s, 0.28f),
            onPrimaryContainer = tone(hue, s * 0.7f, 0.92f),
            secondary = tone(hue, s * 0.6f, 0.66f),
            onSecondary = tone(hue, s * 0.5f, 0.12f),
            secondaryContainer = tone(hue, s * 0.55f, 0.26f),
            onSecondaryContainer = tone(hue, s * 0.5f, 0.90f),
            tertiary = tone(hue, s * 0.9f, 0.72f),
            onTertiary = tone(hue, s * 0.6f, 0.12f),
            tertiaryContainer = tone(hue, s * 0.8f, 0.28f),
            onTertiaryContainer = tone(hue, s * 0.6f, 0.90f),
            background = background,
            onBackground = onBackground,
            surface = background,
            onSurface = onBackground,
            surfaceTint = primary,
            surfaceVariant = tone(hue, s * 0.25f, 0.17f),
            onSurfaceVariant = tone(hue, s * 0.18f, 0.78f),
            surfaceContainerLowest = tone(hue, s * 0.25f, 0.04f),
            surfaceContainerLow = tone(hue, s * 0.25f, 0.08f),
            surfaceContainer = tone(hue, s * 0.25f, 0.10f),
            surfaceContainerHigh = tone(hue, s * 0.22f, 0.14f),
            surfaceContainerHighest = tone(hue, s * 0.20f, 0.18f),
            outline = tone(hue, s * 0.15f, 0.55f),
            outlineVariant = tone(hue, s * 0.18f, 0.28f),
        )

    val secondary =
        secondarySeed?.let { seed ->
            val (sh, ss, sl) = Color(seed or 0xFF000000.toInt()).toHsl()
            Color.hsl(
                blendHue(sh, hue, 0.5f),
                ss.coerceAtLeast(s * 0.7f).coerceAtMost(0.9f),
                sl.coerceIn(0.34f, 0.56f),
            )
        } ?: tone(hue, s, 0.40f)

    return ArtworkColors(
        scheme = scheme,
        gradientHigh = tone(hue, (s * 0.85f).coerceAtMost(0.65f), 0.30f),
        gradientLow = tone(hue, s * 0.45f, 0.04f),
        accents = listOf(tone(hue, s, 0.72f), secondary),
        blobSeed = primarySeed,
    )
}

fun ArtworkColors.gradientColorAt(fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    val base = lerp(gradientHigh, gradientLow, t)
    val fade = ((t - GRADIENT_FADE_START) / (1f - GRADIENT_FADE_START)).coerceIn(0f, 1f)
    return lerp(base, Color.Black, fade)
}

private fun blendHue(
    from: Float,
    to: Float,
    fraction: Float,
): Float {
    val delta = ((to - from + 540f) % 360f) - 180f
    return (from + delta * fraction + 360f) % 360f
}

@Composable
fun rememberArtworkColors(ref: CoverArtRef?): ArtworkColors? {
    val seeds by rememberArtworkSeed(ref)
    return remember(seeds) { seeds?.let { (primary, secondary) -> artworkColorsFromSeeds(primary, secondary) } }
}

@Composable
private fun rememberArtworkSeed(ref: CoverArtRef?): State<Pair<Int, Int?>?> {
    val context = LocalPlatformContext.current
    return produceState<Pair<Int, Int?>?>(initialValue = null, ref?.cacheKey) {
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
                    image?.let { seedsFrom(it.toBitmap()) }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }
    }
}

private fun seedsFrom(bitmap: Bitmap): Pair<Int, Int?>? {
    val palette = Palette.from(bitmap).generate()
    val primary =
        palette.vibrantSwatch
            ?: palette.lightVibrantSwatch
            ?: palette.darkVibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch
            ?: return null
    val primaryColor = Color(primary.rgb or 0xFF000000.toInt())
    val secondary =
        listOfNotNull(
            palette.vibrantSwatch,
            palette.lightVibrantSwatch,
            palette.darkVibrantSwatch,
            palette.mutedSwatch,
            palette.lightMutedSwatch,
            palette.darkMutedSwatch,
            palette.dominantSwatch,
        ).map { it.rgb }
            .distinct()
            .filter { it != primary.rgb }
            .maxByOrNull { distinctness(primaryColor, Color(it or 0xFF000000.toInt())) }
    return primary.rgb to secondary
}

private fun distinctness(
    a: Color,
    b: Color,
): Float {
    val ha = a.toHsl().first
    val hb = b.toHsl().first
    val hueDistance = minOf(abs(ha - hb), 360f - abs(ha - hb)) / 180f
    return hueDistance + abs(a.luminance() - b.luminance()) * 2f
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
    val seed = colors?.blobSeed ?: 0

    Canvas(modifier) {
        drawRect(
            brush =
                Brush.linearGradient(
                    colors = listOf(high, low),
                    start = Offset(size.width * 0.2f, 0f),
                    end = Offset(size.width * 0.8f, size.height),
                ),
        )
        if (accents.isNotEmpty()) {
            val random = Random(seed)
            val zone = size.height * BLOB_ZONE
            repeat(4) { index ->
                val center =
                    Offset(
                        size.width * (0.10f + random.nextFloat() * 0.80f),
                        zone * random.nextFloat(),
                    )
                val radius = size.width * (0.45f + random.nextFloat() * 0.35f)
                val alpha = 0.50f + random.nextFloat() * 0.32f
                val accent = accents[index % accents.size]
                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = alpha), accent.copy(alpha = 0f)),
                            center = center,
                            radius = radius,
                        ),
                )
            }
        }
        drawRect(
            brush =
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0f to Color.Transparent,
                            GRADIENT_FADE_START to Color.Transparent,
                            1f to Color.Black,
                        ),
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
