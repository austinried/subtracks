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
    a.copy(
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
        background = lerp(a.background, b.background, t),
        onBackground = lerp(a.onBackground, b.onBackground, t),
        surface = lerp(a.surface, b.surface, t),
        onSurface = lerp(a.onSurface, b.onSurface, t),
        surfaceVariant = lerp(a.surfaceVariant, b.surfaceVariant, t),
        onSurfaceVariant = lerp(a.onSurfaceVariant, b.onSurfaceVariant, t),
        surfaceTint = lerp(a.surfaceTint, b.surfaceTint, t),
        inverseSurface = lerp(a.inverseSurface, b.inverseSurface, t),
        inverseOnSurface = lerp(a.inverseOnSurface, b.inverseOnSurface, t),
        error = lerp(a.error, b.error, t),
        onError = lerp(a.onError, b.onError, t),
        errorContainer = lerp(a.errorContainer, b.errorContainer, t),
        onErrorContainer = lerp(a.onErrorContainer, b.onErrorContainer, t),
        outline = lerp(a.outline, b.outline, t),
        outlineVariant = lerp(a.outlineVariant, b.outlineVariant, t),
        scrim = lerp(a.scrim, b.scrim, t),
        surfaceBright = lerp(a.surfaceBright, b.surfaceBright, t),
        surfaceDim = lerp(a.surfaceDim, b.surfaceDim, t),
        surfaceContainer = lerp(a.surfaceContainer, b.surfaceContainer, t),
        surfaceContainerHigh = lerp(a.surfaceContainerHigh, b.surfaceContainerHigh, t),
        surfaceContainerHighest = lerp(a.surfaceContainerHighest, b.surfaceContainerHighest, t),
        surfaceContainerLow = lerp(a.surfaceContainerLow, b.surfaceContainerLow, t),
        surfaceContainerLowest = lerp(a.surfaceContainerLowest, b.surfaceContainerLowest, t),
        primaryFixed = lerp(a.primaryFixed, b.primaryFixed, t),
        primaryFixedDim = lerp(a.primaryFixedDim, b.primaryFixedDim, t),
        onPrimaryFixed = lerp(a.onPrimaryFixed, b.onPrimaryFixed, t),
        onPrimaryFixedVariant = lerp(a.onPrimaryFixedVariant, b.onPrimaryFixedVariant, t),
        secondaryFixed = lerp(a.secondaryFixed, b.secondaryFixed, t),
        secondaryFixedDim = lerp(a.secondaryFixedDim, b.secondaryFixedDim, t),
        onSecondaryFixed = lerp(a.onSecondaryFixed, b.onSecondaryFixed, t),
        onSecondaryFixedVariant = lerp(a.onSecondaryFixedVariant, b.onSecondaryFixedVariant, t),
        tertiaryFixed = lerp(a.tertiaryFixed, b.tertiaryFixed, t),
        tertiaryFixedDim = lerp(a.tertiaryFixedDim, b.tertiaryFixedDim, t),
        onTertiaryFixed = lerp(a.onTertiaryFixed, b.onTertiaryFixed, t),
        onTertiaryFixedVariant = lerp(a.onTertiaryFixedVariant, b.onTertiaryFixedVariant, t),
    )
