package com.subtracks.ui.playback

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.size.Dimension
import com.subtracks.data.model.CoverArtRef
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
class NowPlayingArtworkTest {
    @After
    fun resetImageLoader() {
        SingletonImageLoader.reset()
    }

    @Test
    fun prefetchingTheNextOriginalIsBoundedToTheScreen() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val ref = CoverArtRef(url = "http://example.test/art", cacheKey = "1:art:false")

            val request = prefetchImageRequest(context, ref)
            val resolved = request.sizeResolver.size()
            val width = (resolved.width as Dimension.Pixels).px

            val metrics = context.resources.displayMetrics
            assertEquals(maxOf(metrics.widthPixels, metrics.heightPixels), width)
            assertEquals(ref.cacheKey, request.memoryCacheKey)
            assertEquals(ref.cacheKey, request.diskCacheKey)
        }
}
