package com.subtracks.ui.theme

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtworkNameBandTest {
    private fun bitmap(bottom: (Int, Int) -> Int): Bitmap =
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until 64) {
                for (x in 0 until 64) {
                    setPixel(x, y, if (y >= 46) bottom(x, y) else Color.rgb(20, 20, 20))
                }
            }
        }

    @Test
    fun darkCalmBandNeedsNoScrim() {
        assertFalse(ArtworkSeedCache.bottomBandBusy(bitmap { _, _ -> Color.rgb(24, 20, 34) }))
    }

    @Test
    fun brightBandNeedsAScrim() {
        assertTrue(ArtworkSeedCache.bottomBandBusy(bitmap { _, _ -> Color.rgb(235, 230, 220) }))
    }

    @Test
    fun noisyBandNeedsAScrim() {
        assertTrue(ArtworkSeedCache.bottomBandBusy(bitmap { x, y -> if ((x + y) % 2 == 0) Color.WHITE else Color.BLACK }))
    }
}
