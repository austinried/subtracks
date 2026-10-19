package com.subtracks.ui.library

import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song
import com.subtracks.ui.components.artistSubtitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongArtistSubtitleTest {
    @Test
    fun pluralArtistsAreJoinedWithDots() {
        assertEquals("One · Two", song(artist = null, artists = listOf(ArtistRef("1", "One"), ArtistRef("2", "Two"))).artistSubtitle())
    }

    @Test
    fun singularArtistIsListedFirstWhenItIsAlsoPlural() {
        assertEquals(
            "Two · One · Three",
            song(artist = "Two", artists = listOf(ArtistRef("1", "One"), ArtistRef("2", "Two"), ArtistRef("3", "Three")))
                .artistSubtitle(),
        )
    }

    @Test
    fun singularArtistIsUsedWhenNoPluralArtistsExist() {
        assertEquals("One", song(artist = "One", artists = emptyList()).artistSubtitle())
    }

    @Test
    fun noArtistGivesNoSubtitle() {
        assertNull(song(artist = null, artists = emptyList()).artistSubtitle())
    }

    private fun song(
        artist: String?,
        artists: List<ArtistRef>,
    ) = Song(
        sourceId = 1,
        id = "s1",
        albumId = "al1",
        artistId = "ar1",
        title = "Song",
        album = "Album",
        artist = artist,
        duration = 100,
        track = 1,
        disc = 1,
        starred = null,
        artists = artists,
    )
}
