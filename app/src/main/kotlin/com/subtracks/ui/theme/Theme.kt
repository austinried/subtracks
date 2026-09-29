package com.subtracks.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun SubtracksTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme =
        remember(context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context)
            } else {
                darkColorScheme()
            }
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SubtracksTypography,
        content = content,
    )
}
