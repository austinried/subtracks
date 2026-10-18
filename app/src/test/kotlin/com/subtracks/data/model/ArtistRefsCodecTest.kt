package com.subtracks.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ArtistRefsCodecTest {
    @Test
    fun roundTripsNamesWithTheOldSeparatorsAndColons() {
        val refs =
            listOf(
                ArtistRef("ar1", "Name\u001Ewith\u001Fseparators"),
                ArtistRef("ar:2", "1:Colon Artist"),
                ArtistRef("", ""),
            )

        assertEquals(refs, ArtistRefsCodec.decode(ArtistRefsCodec.encode(refs)))
    }

    @Test
    fun decodesEmptyAndMalformedValuesAsEmpty() {
        assertEquals(emptyList<ArtistRef>(), ArtistRefsCodec.decode(""))
        assertEquals(emptyList<ArtistRef>(), ArtistRefsCodec.decode("not a ref"))
        assertEquals(emptyList<ArtistRef>(), ArtistRefsCodec.decode("9:short4:name"))
    }
}
