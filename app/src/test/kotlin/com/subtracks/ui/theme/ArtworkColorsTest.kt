package com.subtracks.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun placeholderColourDependsOnTheNameAndIsNotGrey() {
        assertEquals(0, placeholderSeed(""))
        assertNotEquals(placeholderSeed("Kid A"), placeholderSeed("Amnesiac"))
        val (_, saturation, _) = Color(placeholderSeed("Kid A")).toHsl()
        assertTrue("placeholder should carry colour", saturation > 0f)
    }

    @Test
    fun independentOfAlphaBits() {
        assertEquals(
            artworkColorsFromSeed(0xFF3A7BD5.toInt()).scheme.primary,
            artworkColorsFromSeed(0x3A7BD5).scheme.primary,
        )
    }

    @Test
    fun buttonStandsOutFromGradient() {
        for (seed in seeds) {
            val colors = artworkColorsFromSeed(seed)
            val primary = colors.scheme.primary
            assertTrue("primary vs gradientHigh for $seed", contrast(primary, colors.gradientHigh) >= 2f)
            assertTrue("primary vs gradientLow for $seed", contrast(primary, colors.gradientLow) >= 2f)
            for (accent in colors.accents) {
                assertTrue("accent dimmer than primary for $seed", primary.luminance() > accent.luminance())
            }
        }
    }

    @Test
    fun secondaryTextReadsOnGradient() {
        for (seed in seeds) {
            val colors = artworkColorsFromSeed(seed)
            val backdrop =
                (listOf(colors.gradientHigh, colors.gradientLow) + colors.accents)
                    .maxBy { it.luminance() }
            assertTrue("onSurfaceVariant for $seed", contrast(colors.scheme.onSurfaceVariant, backdrop) >= 3f)
        }
    }

    @Test
    fun warmArtworkKeepsThePlayerSurfaceDark() {
        val warm = artworkColorsFromSeed(0xFFE6C822.toInt())
        assertTrue(playerSurfaceColor(warm).luminance() < 0.1f)
    }

    @Test
    fun warmSaturationDampingIsContinuousAroundTheHueCircle() {
        assertEquals(surfaceSaturationFactor(0f), surfaceSaturationFactor(359.9f), 0.01f)
        assertTrue(surfaceSaturationFactor(60f) < surfaceSaturationFactor(240f))
        assertTrue(surfaceSaturationFactor(359f) < surfaceSaturationFactor(330f))
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
