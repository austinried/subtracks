package com.subtracks.ui.components

import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistArtTest {
    @Test
    fun providerImageWinsOverArtistAndAlbumArt() {
        val calls = mutableListOf<String>()
        val art =
            resolveArtistArt(
                artist = artist(artistImageUrl = "https://img/artist"),
                coverArt = { id, _ -> id?.let { CoverArtRef(it, "cache:$it") } },
                artistImage = { id, url ->
                    calls += "image:$id:$url"
                    CoverArtRef("https://img/artist", "cache:image:$id")
                },
                offline = false,
                forIcon = true,
            )

        assertEquals(CoverArtRef("https://img/artist", "cache:image:ar1"), art.image)
        assertTrue(art.covers.isEmpty())
        assertEquals(listOf("image:ar1:https://img/artist"), calls)
    }

    @Test
    fun artistCoverArtWinsOverMaterializedAlbumArt() {
        val thumbnails = mutableListOf<Boolean>()
        val art =
            resolveArtistArt(
                artist = artist(coverArt = "own", coverArts = listOf("a", "b")),
                coverArt = { id, thumbnail ->
                    thumbnails += thumbnail
                    id?.let { CoverArtRef(it, "cache:$it:$thumbnail") }
                },
                artistImage = { _, _ -> null },
                offline = false,
                forIcon = true,
            )

        assertEquals(CoverArtRef("own", "cache:own:true"), art.image)
        assertTrue(art.covers.isEmpty())
        assertTrue(thumbnails.all { it })
    }

    @Test
    fun iconUsesThumbnailsAndLimitsTheGridToFourCovers() {
        val thumbnails = mutableListOf<Boolean>()
        val art =
            resolveArtistArt(
                artist = artist(coverArts = listOf("a", "b", "c", "d", "e")),
                coverArt = { id, thumbnail ->
                    thumbnails += thumbnail
                    id?.let { CoverArtRef(it, "cache:$it") }
                },
                artistImage = { _, _ -> null },
                offline = false,
                forIcon = true,
            )

        assertEquals(listOf("a", "b", "c", "d"), art.covers.map { it.url })
        assertEquals(listOf(true, true, true, true), thumbnails)
    }

    @Test
    fun aSingleHeroCoverLoadsFullSizeWhileMultipleCoversUseThumbnails() {
        val thumbnails = mutableListOf<Boolean>()
        val coverArt = { id: String?, thumbnail: Boolean ->
            thumbnails += thumbnail
            id?.let { CoverArtRef(it, "cache:$it") }
        }
        val artistImage: (String, String?) -> CoverArtRef? = { _, _ -> null }

        val single =
            resolveArtistArt(
                artist(coverArts = listOf("a")),
                coverArt,
                artistImage,
                offline = false,
                forIcon = false,
            )
        assertEquals(listOf(true, false), thumbnails)
        assertEquals("a", single.thumbnail?.url)

        thumbnails.clear()
        resolveArtistArt(artist(coverArts = listOf("a", "b")), coverArt, artistImage, offline = false, forIcon = false)
        assertEquals(listOf(true, true), thumbnails)
    }

    @Test
    fun offlineUsesDownloadedCoversAndNeverOnlineOnlyOnes() {
        val requested = mutableListOf<String>()
        val art =
            resolveArtistArt(
                artist =
                    artist(
                        coverArt = "online-own",
                        coverArts = listOf("online-a", "online-b"),
                        downloadedCoverArts = listOf("downloaded-a", "downloaded-b"),
                    ),
                coverArt = { id, _ ->
                    requested += id ?: "null"
                    id?.takeIf { it.startsWith("downloaded") }?.let { CoverArtRef(it, "cache:$it") }
                },
                artistImage = { _, _ -> null },
                offline = true,
                forIcon = true,
            )

        assertEquals(listOf("downloaded-a", "downloaded-b"), art.covers.map { it.url })
        assertEquals(setOf("online-own", "downloaded-a", "downloaded-b"), requested.toSet())
    }

    @Test
    fun theProviderAndArtistImagesCarryTheNextSourceAsAFallback() {
        val provider =
            resolveArtistArt(
                artist = artist(coverArt = "own", artistImageUrl = "https://img/artist", coverArts = listOf("a", "b")),
                coverArt = { id, _ -> id?.let { CoverArtRef(it, "cache:$it") } },
                artistImage = { _, _ -> CoverArtRef("https://img/artist", "cache:image") },
                offline = false,
                forIcon = true,
            )

        assertEquals(CoverArtRef("own", "cache:own"), provider.fallback?.image)
        assertEquals(
            listOf("a", "b"),
            provider.fallback
                ?.fallback
                ?.covers
                ?.map { it.url },
        )

        val own =
            resolveArtistArt(
                artist = artist(coverArt = "own", coverArts = listOf("a", "b")),
                coverArt = { id, _ -> id?.let { CoverArtRef(it, "cache:$it") } },
                artistImage = { _, _ -> null },
                offline = false,
                forIcon = true,
            )

        assertEquals(listOf("a", "b"), own.fallback?.covers?.map { it.url })
    }

    @Test
    fun noArtResolvesToAnEmptySelectionInsteadOfAStaleFallback() {
        val art =
            resolveArtistArt(
                artist = artist(coverArt = "online-own", coverArts = listOf("a")),
                coverArt = { _, _ -> null },
                artistImage = { _, _ -> null },
                offline = true,
                forIcon = true,
            )

        assertNull(art.image)
        assertTrue(art.covers.isEmpty())
    }

    private fun artist(
        coverArt: String? = null,
        artistImageUrl: String? = null,
        coverArts: List<String> = emptyList(),
        downloadedCoverArts: List<String> = emptyList(),
    ) = Artist(
        sourceId = 1,
        id = "ar1",
        name = "Artist",
        albumCount = 0,
        starred = null,
        coverArt = coverArt,
        artistImageUrl = artistImageUrl,
        coverArts = coverArts,
        downloadedCoverArts = downloadedCoverArts,
    )
}
