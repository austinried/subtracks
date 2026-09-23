package com.subtracks.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtworkColorsTest {
    private val seeds =
        listOf(
            0xFF3A7BD5.toInt(),
            0xFFD53A3A.toInt(),
            0xFF3AD55F.toInt(),
            0xFFB0B0B0.toInt(),
            0x00000000,
        )

    @Test
    fun darkBackgroundWithReadableText() {
        for (seed in seeds) {
            val scheme = artworkColorsFromSeed(seed).scheme
            assertTrue("background for $seed", scheme.background.luminance() < 0.1f)
            assertTrue("onBackground for $seed", contrast(scheme.background, scheme.onBackground) >= 4.5f)
            assertTrue("primary text for $seed", contrast(scheme.primary, scheme.onPrimary) >= 3f)
            assertTrue("onSurface for $seed", contrast(scheme.surface, scheme.onSurface) >= 4.5f)
        }
    }

    @Test
    fun independentOfAlphaBits() {
        assertEquals(
            artworkColorsFromSeed(0xFF3A7BD5.toInt()).scheme.primary,
            artworkColorsFromSeed(0x3A7BD5).scheme.primary,
        )
    }

    private fun contrast(
        a: Color,
        b: Color,
    ): Float {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05f) / (lo + 0.05f)
    }
}
