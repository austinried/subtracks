package com.subtracks.ui.library

import com.subtracks.data.model.Artist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtistDetailArtTest {
    @Test
    fun aCreditedArtistWithoutARowUsesTheCreditCovers() {
        val resolved =
            resolveArtistArtInput(
                ArtistArtInput(
                    artist = null,
                    artistId = "ar1",
                    offline = false,
                    sourceId = 7,
                    covers = listOf("a", "b"),
                ),
            )

        assertEquals(7L, resolved?.sourceId)
        assertEquals("ar1", resolved?.id)
        assertEquals(listOf("a", "b"), resolved?.coverArts)
    }

    @Test
    fun anOfflineArtistKeepsItsDownloadedCovers() {
        val resolved =
            resolveArtistArtInput(
                ArtistArtInput(
                    artist = artist(coverArts = listOf("online"), downloadedCoverArts = listOf("downloaded")),
                    artistId = "ar1",
                    offline = true,
                    sourceId = 7,
                    covers = listOf("online", "other"),
                ),
            )

        assertEquals(listOf("online", "other"), resolved?.coverArts)
        assertEquals(listOf("downloaded"), resolved?.downloadedCoverArts)
    }

    @Test
    fun anOfflineArtistWithoutDownloadedCoversFallsBackToTheCreditCovers() {
        val resolved =
            resolveArtistArtInput(
                ArtistArtInput(
                    artist = artist(downloadedCoverArts = emptyList()),
                    artistId = "ar1",
                    offline = true,
                    sourceId = 7,
                    covers = listOf("a", "b"),
                ),
            )

        assertEquals(listOf("a", "b"), resolved?.downloadedCoverArts)
    }

    @Test
    fun anArtistWithMaterializedCoversIsKeptWhenTheCreditQueryIsEmpty() {
        val original = artist(coverArts = listOf("materialized"))

        val resolved =
            resolveArtistArtInput(
                ArtistArtInput(
                    artist = original,
                    artistId = "ar1",
                    offline = false,
                    sourceId = 7,
                    covers = emptyList(),
                ),
            )

        assertEquals(original, resolved)
    }

    @Test
    fun noArtistAndNoCoversClearsTheArt() {
        assertNull(
            resolveArtistArtInput(
                ArtistArtInput(
                    artist = null,
                    artistId = "ar1",
                    offline = false,
                    sourceId = 7,
                    covers = emptyList(),
                ),
            ),
        )
    }

    private fun artist(
        coverArts: List<String> = emptyList(),
        downloadedCoverArts: List<String> = emptyList(),
    ) = Artist(
        sourceId = 7,
        id = "ar1",
        name = "Artist",
        albumCount = 1,
        starred = null,
        coverArts = coverArts,
        downloadedCoverArts = downloadedCoverArts,
    )
}
