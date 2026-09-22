package com.subtracks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

val SubtracksTypography =
    Typography().let { base ->
        base.copy(
            headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.W800),
            headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.W700),
            headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.W600),
        )
    }
