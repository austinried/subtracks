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
import androidx.compose.ui.graphics.TileMode
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
private const val PERIOD_SCREENS = 2f
private const val BLOB_ZONE = 0.62f
private const val BLOB_COUNT = 4
private const val ACCENT_MAX_LUMINANCE = 0.34f
private const val HERO_DARKEN_MAX = 1.0f
private const val DARKEN_START_SCREENS = 0.5f
private const val DARKEN_END_SCREENS = 1.5f
private const val DARKEN_KNEE = 0.2f

data class ArtworkColors(
    val scheme: ColorScheme,
    val gradientHigh: Color,
    val gradientLow: Color,
    val accents: List<Color>,
    val darkPrimary: Color,
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
        gradientLow = tone(hue, s * 0.5f, 0.16f),
        accents =
            listOf(
                tone(hue, s, 0.72f).withMaxLuminance(ACCENT_MAX_LUMINANCE),
                secondary.withMaxLuminance(ACCENT_MAX_LUMINANCE),
            ),
        darkPrimary = tone(hue, s * 0.8f, 0.10f),
        blobSeed = primarySeed,
    )
}

private fun Color.withMaxLuminance(max: Float): Color {
    if (luminance() <= max) return this
    val (h, s, initial) = toHsl()
    var lightness = initial
    var color = this
    var guard = 0
    while (color.luminance() > max && guard++ < 30) {
        lightness = (lightness - 0.02f).coerceAtLeast(0.05f)
        color = Color.hsl(h, s, lightness)
    }
    return color
}

fun ArtworkColors.gradientColorAt(fraction: Float): Color {
    if (fraction.isNaN()) return gradientHigh
    val f = fraction.mod(1f)
    return if (f < 0.5f) {
        lerp(gradientHigh, gradientLow, f * 2f)
    } else {
        lerp(gradientLow, gradientHigh, (f - 0.5f) * 2f)
    }
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
    scrollPx: () -> Float,
    modifier: Modifier = Modifier,
) {
    val high = colors?.gradientHigh ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val low = colors?.gradientLow ?: MaterialTheme.colorScheme.background
    val accents = colors?.accents.orEmpty()
    val dark = colors?.darkPrimary ?: MaterialTheme.colorScheme.background
    val seed = colors?.blobSeed ?: 0

    Canvas(modifier) {
        val period = size.height * PERIOD_SCREENS
        if (period <= 0f) return@Canvas
        val scroll = scrollPx()
        val phase = scroll.mod(period)
        drawRect(
            brush =
                Brush.verticalGradient(
                    colorStops = arrayOf(0f to high, 0.5f to low, 1f to high),
                    startY = -phase,
                    endY = -phase + period,
                    tileMode = TileMode.Repeated,
                ),
        )
        if (accents.isNotEmpty()) {
            val random = Random(seed)
            repeat(BLOB_COUNT) { index ->
                val centerX = size.width * (0.10f + random.nextFloat() * 0.80f)
                val baseY = period * BLOB_ZONE * random.nextFloat()
                val radius = size.width * (0.45f + random.nextFloat() * 0.35f)
                val alpha = 0.50f + random.nextFloat() * 0.32f
                val accent = accents[index % accents.size]
                for (shift in -1..1) {
                    val centerY = baseY + shift * period - phase
                    if (centerY + radius < 0f || centerY - radius > size.height) continue
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = alpha), accent.copy(alpha = 0f)),
                                center = Offset(centerX, centerY),
                                radius = radius,
                            ),
                    )
                }
            }
        }
        val darkenStart = size.height * DARKEN_START_SCREENS - scroll
        val darkenEnd = size.height * DARKEN_END_SCREENS - scroll
        drawRect(
            brush =
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0f to Color.Transparent,
                            DARKEN_KNEE to dark.copy(alpha = HERO_DARKEN_MAX * 0.5f),
                            1f to dark.copy(alpha = HERO_DARKEN_MAX),
                        ),
                    startY = darkenStart,
                    endY = darkenEnd,
                    tileMode = TileMode.Clamp,
                ),
        )
    }
}

fun heroDarkenAt(
    contentY: Float,
    screenHeightPx: Float,
): Float {
    val start = screenHeightPx * DARKEN_START_SCREENS
    val end = screenHeightPx * DARKEN_END_SCREENS
    val fraction = ((contentY - start) / (end - start)).coerceIn(0f, 1f)
    return if (fraction <= DARKEN_KNEE) {
        HERO_DARKEN_MAX * 0.5f * (fraction / DARKEN_KNEE)
    } else {
        HERO_DARKEN_MAX * 0.5f + HERO_DARKEN_MAX * 0.5f * ((fraction - DARKEN_KNEE) / (1f - DARKEN_KNEE))
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
