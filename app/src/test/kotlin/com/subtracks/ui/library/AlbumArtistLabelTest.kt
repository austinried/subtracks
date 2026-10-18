package com.subtracks.ui.library

import com.subtracks.data.model.Album
import com.subtracks.data.model.ArtistRef
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumArtistLabelTest {
    @Test
    fun creditsBothTheSingularIdAndEveryPluralRef() {
        val album =
            album(
                artistId = "a1",
                artists = listOf(ArtistRef("a2", "B")),
            )

        assertTrue(isAlbumArtist(listOf(album), "a1"))
        assertTrue(isAlbumArtist(listOf(album), "a2"))
        assertFalse(isAlbumArtist(listOf(album), "a3"))
    }

    private fun album(
        artistId: String?,
        artists: List<ArtistRef>,
    ) = Album(
        sourceId = 1,
        id = "al1",
        artistId = artistId,
        name = "Album",
        albumArtist = "Various Artists",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
        artists = artists,
    )
}
