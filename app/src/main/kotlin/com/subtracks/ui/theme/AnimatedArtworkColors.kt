package com.subtracks.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp

const val ARTWORK_THEME_TRANSITION_MS = 600

private val baseArtworkColors =
    ArtworkColors(
        scheme = SubtracksColorScheme,
        gradientHigh = SubtracksColorScheme.surfaceContainerHigh,
        gradientLow = SubtracksColorScheme.background,
        accents = listOf(SubtracksColorScheme.surfaceContainerHighest, SubtracksColorScheme.surfaceContainerLow),
        darkPrimary = SubtracksColorScheme.background,
        blobSeed = 0,
    )

@Composable
fun rememberAnimatedArtworkColors(
    target: ArtworkColors?,
    durationMillis: Int = ARTWORK_THEME_TRANSITION_MS,
): ArtworkColors {
    var from by remember { mutableStateOf(target ?: baseArtworkColors) }
    var to by remember { mutableStateOf(target ?: baseArtworkColors) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(target, durationMillis) {
        val next = target ?: baseArtworkColors
        if (next !== to) {
            from = lerpArtworkColors(from, to, progress.value)
            to = next
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = durationMillis))
        }
    }

    val fraction = progress.value
    return remember(from, to, fraction) { lerpArtworkColors(from, to, fraction) }
}

private fun lerpArtworkColors(
    a: ArtworkColors,
    b: ArtworkColors,
    t: Float,
): ArtworkColors =
    ArtworkColors(
        scheme = lerpScheme(a.scheme, b.scheme, t),
        gradientHigh = lerp(a.gradientHigh, b.gradientHigh, t),
        gradientLow = lerp(a.gradientLow, b.gradientLow, t),
        accents = b.accents.mapIndexed { index, color -> lerp(a.accents.getOrElse(index) { color }, color, t) },
        darkPrimary = lerp(a.darkPrimary, b.darkPrimary, t),
        blobSeed = b.blobSeed,
    )

private fun lerpScheme(
    a: ColorScheme,
    b: ColorScheme,
    t: Float,
): ColorScheme =
    b.copy(
        primary = lerp(a.primary, b.primary, t),
        onPrimary = lerp(a.onPrimary, b.onPrimary, t),
        primaryContainer = lerp(a.primaryContainer, b.primaryContainer, t),
        onPrimaryContainer = lerp(a.onPrimaryContainer, b.onPrimaryContainer, t),
        inversePrimary = lerp(a.inversePrimary, b.inversePrimary, t),
        secondary = lerp(a.secondary, b.secondary, t),
        onSecondary = lerp(a.onSecondary, b.onSecondary, t),
        secondaryContainer = lerp(a.secondaryContainer, b.secondaryContainer, t),
        onSecondaryContainer = lerp(a.onSecondaryContainer, b.onSecondaryContainer, t),
        tertiary = lerp(a.tertiary, b.tertiary, t),
        onTertiary = lerp(a.onTertiary, b.onTertiary, t),
        tertiaryContainer = lerp(a.tertiaryContainer, b.tertiaryContainer, t),
        onTertiaryContainer = lerp(a.onTertiaryContainer, b.onTertiaryContainer, t),
    )
