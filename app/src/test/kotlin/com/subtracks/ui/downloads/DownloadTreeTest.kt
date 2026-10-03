package com.subtracks.ui.downloads

import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.DownloadedSong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadTreeTest {
    @Test
    fun songsAreGroupedByArtistAndAlbumWithSizesSummed() {
        val tree =
            buildTree(
                listOf(
                    song("s1", "One", "ar1", "Artist", "al1", "First", size = 10),
                    song("s2", "Two", "ar1", "Artist", "al1", "First", size = 20),
                    song("s3", "Three", "ar1", "Artist", "al2", "Second", size = 5),
                    song("s4", "Four", "ar2", "Other", "al3", "Third", size = 1),
                ),
            )

        assertEquals(listOf("Artist", "Other"), tree.artists.map { it.name })
        val artist = tree.artists.first()
        assertEquals(35L, artist.bytes)
        assertEquals(listOf("First", "Second"), artist.albums.map { it.name })
        assertEquals(30L, artist.albums.first().bytes)
        assertEquals(
            listOf("One", "Two"),
            artist.albums
                .first()
                .songs
                .map { it.title },
        )
        assertEquals(4, tree.songs)
        assertEquals(36L, tree.bytes)
    }

    @Test
    fun aSongWithNoArtistOrAlbumIsStillShown() {
        val tree = buildTree(listOf(song("s1", "One", null, null, null, null, size = 3)))

        val artist = tree.artists.single()
        assertEquals("Unknown artist", artist.name)
        assertEquals("", artist.id)
        assertEquals("Unknown album", artist.albums.single().name)
    }

    @Test
    fun nodeProgressCountsCompletedTracksRatherThanBytes() {
        val songs =
            listOf(
                song("s1", "One", "ar1", "Artist", "al1", "Album", size = 100),
                song("s2", "Two", "ar1", "Artist", "al1", "Album", size = 50)
                    .copy(status = DownloadStatus.Running, bytes = 50, total = 100),
            )

        assertEquals(0.75f, nodeProgress(songs)!!, 0.0001f)
        assertNull(nodeProgress(listOf(song("s3", "Three", "ar1", "Artist", "al1", "Album", size = 1))))
    }

    private fun song(
        id: String,
        title: String,
        artistId: String?,
        artistName: String?,
        albumId: String?,
        albumName: String?,
        size: Long,
    ) = DownloadedSong(
        songId = id,
        sourceId = 1,
        title = title,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        status = DownloadStatus.Completed,
        size = size,
    )
}
