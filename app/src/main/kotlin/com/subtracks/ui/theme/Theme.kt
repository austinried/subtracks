package com.subtracks.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SubtracksDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

@Composable
fun SubtracksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SubtracksDarkColorScheme,
        typography = SubtracksTypography,
        content = content,
    )
}
