package com.subtracks.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.util.concurrent.atomic.AtomicInteger

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CoverArtReloadTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val requests = AtomicInteger(0)

    @Before
    fun installImageLoader() {
        SingletonImageLoader.reset()
        val image = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }.asImage()
        val engine =
            FakeImageLoaderEngine
                .Builder()
                .requestTransformer {
                    requests.incrementAndGet()
                    it
                }.default(image)
                .build()
        SingletonImageLoader.setUnsafe(
            ImageLoader
                .Builder(ApplicationProvider.getApplicationContext<Context>())
                .components { add(engine) }
                .build(),
        )
    }

    @After
    fun resetImageLoader() {
        SingletonImageLoader.reset()
    }

    @Test
    fun aNewSaltedUrlForTheSameArtworkDoesNotRestartTheLoad() {
        val refState = mutableStateOf(CoverArtRef(url = "http://host/art?id=1&s=one", cacheKey = "1:art:true"))
        composeRule.setContent {
            CoverArt(ref = refState.value, name = "Kid A", modifier = Modifier.size(64.dp))
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { requests.get() == 1 }

        refState.value = CoverArtRef(url = "http://host/art?id=1&s=two", cacheKey = "1:art:true")
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        assertEquals(1, requests.get())
    }
}
