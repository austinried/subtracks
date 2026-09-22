package com.subtracks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SubtracksColorScheme =
    darkColorScheme(
        primary = White,
        onPrimary = Black,
        primaryContainer = White,
        onPrimaryContainer = Black,
        secondary = White,
        onSecondary = Black,
        secondaryContainer = White,
        onSecondaryContainer = Black,
        tertiary = White,
        onTertiary = Black,
        background = Black,
        onBackground = White,
        surface = Black,
        onSurface = White,
        surfaceVariant = DarkGrey,
        onSurfaceVariant = LightGrey,
        surfaceContainerLowest = Black,
        surfaceContainerLow = NearBlack,
        surfaceContainer = NearBlack,
        surfaceContainerHigh = DarkGrey,
        surfaceContainerHighest = MidGrey,
        outline = MidGrey,
        outlineVariant = DarkGrey,
    )

@Composable
fun SubtracksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SubtracksColorScheme,
        typography = SubtracksTypography,
        content = content,
    )
}
