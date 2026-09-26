package com.subtracks.playback

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoverArtArtworkTest {
    @Test
    fun artworkUrisRoundTripThroughTheCoverArtId() {
        val coverArtId = "al-123/with space"
        val uri = CoverArtArtwork.uri(coverArtId)

        assertEquals(CoverArtArtwork.SCHEME, uri.scheme)
        assertEquals(coverArtId, CoverArtArtwork.coverArtId(uri))
    }

    @Test
    fun nonCoverArtUrisAreRejected() {
        assertNull(CoverArtArtwork.coverArtId(Uri.parse("https://example.test/art")))
    }
}
