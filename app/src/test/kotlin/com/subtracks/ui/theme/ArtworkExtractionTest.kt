package com.subtracks.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import com.subtracks.data.model.CoverArtRef
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtworkExtractionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val bitmap =
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(30, 120, 200))
        }

    @Before
    fun installImageLoader() {
        ArtworkSeedCache.clear()
        SingletonImageLoader.reset()
        SingletonImageLoader.setUnsafe(
            ImageLoader
                .Builder(ApplicationProvider.getApplicationContext<Context>())
                .components { add(FakeImageLoaderEngine.Builder().intercept("art", bitmap.asImage()).build()) }
                .build(),
        )
    }

    @After
    fun resetImageLoader() {
        ArtworkSeedCache.clear()
        SingletonImageLoader.reset()
    }

    @Test
    fun resolvesColorsFromCoverArt() {
        val ref = CoverArtRef(url = "art", cacheKey = "art-${System.nanoTime()}")
        var colors: ArtworkColors? = null
        composeRule.setContent {
            colors = rememberArtworkColors(ref)
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { colors != null }
        assertNotNull(colors)
        assertTrue(colors!!.scheme.background.luminance() < 0.1f)
    }

    @Test
    fun keepsContentStateWhenArtworkArrives() {
        val artwork = mutableStateOf<ArtworkColors?>(null)
        lateinit var state: MutableState<Int>
        composeRule.setContent {
            SubtracksTheme {
                ArtworkTheme(artwork.value) {
                    state = remember { mutableStateOf(0) }
                    Text(state.value.toString())
                }
            }
        }
        composeRule.runOnIdle { state.value = 42 }
        composeRule.runOnIdle { artwork.value = artworkColorsFromSeed(0xFF3A7BD5.toInt()) }
        composeRule.waitForIdle()
        assertEquals(42, state.value)
    }
}
