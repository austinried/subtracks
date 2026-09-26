package com.subtracks.ui.components

import android.content.Context
import androidx.compose.ui.unit.IntSize
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.CoverArtRef
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoverArtRequestTest {
    @Test
    fun sizedRequestBucketsTheMemoryKeyBySizeAndKeepsTheDiskKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val ref = CoverArtRef(url = "http://example.test/art", cacheKey = "1:art:false")

        val request = imageRequest(context, ref, crossfade = false, size = IntSize(600, 800))

        assertEquals("1:art:false:600x800", request.memoryCacheKey)
        assertEquals(ref.cacheKey, request.diskCacheKey)
    }

    @Test
    fun unsizedRequestKeepsTheIdentityMemoryKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val ref = CoverArtRef(url = "http://example.test/art", cacheKey = "1:art:true")

        val request = imageRequest(context, ref, crossfade = false)

        assertEquals(ref.cacheKey, request.memoryCacheKey)
        assertEquals(ref.cacheKey, request.diskCacheKey)
    }
}
