package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
import com.subtracks.data.model.Disc
import com.subtracks.data.model.Song
import com.subtracks.ui.library.AlbumDetailScreen
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class AlbumDiscsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun multiDiscAlbumShowsDiscHeaders() {
        setContent(listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 1, track = 2), song("s-c", disc = 2, track = 1)))
        assertEquals(1, nodesWithText("Disc 1"))
        assertEquals(1, nodesWithText("Disc 2"))
    }

    @Test
    fun discHeadersUseTheDiscTitleWhenPresent() {
        setContent(
            listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 2, track = 1)),
            discs = listOf(disc(1, "The Calm"), disc(2, "The Storm")),
        )
        assertEquals(1, nodesWithText("The Calm"))
        assertEquals(1, nodesWithText("The Storm"))
        assertEquals(0, nodesWithText("Disc 1"))
    }

    @Test
    fun repeatedDiscTitlesGetTheDiscNumber() {
        setContent(
            listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 2, track = 1)),
            discs = listOf(disc(1, "Live"), disc(2, "Live")),
        )
        assertEquals(1, nodesWithText("Live: Disc 1"))
        assertEquals(1, nodesWithText("Live: Disc 2"))
    }

    @Test
    fun discHeadersFallBackToTheDiscNumber() {
        setContent(listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 2, track = 1)))
        assertEquals(1, nodesWithText("Disc 1"))
        assertEquals(1, nodesWithText("Disc 2"))
    }

    @Test
    fun singleDiscAlbumShowsItsTitleWhenPresent() {
        setContent(
            listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 1, track = 2)),
            discs = listOf(disc(1, "The Calm")),
        )
        assertEquals(1, nodesWithText("The Calm"))
        assertEquals(0, nodesWithText("Disc 1"))
    }

    @Test
    fun albumTracksShowTheirTrackNumbers() {
        setContent(listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 1, track = 2)))
        assertEquals(1, nodesWithText("1"))
        assertEquals(1, nodesWithText("2"))
    }

    @Test
    fun singleDiscAlbumShowsNoDiscHeaders() {
        setContent(listOf(song("s-a", disc = 1, track = 1), song("s-b", disc = 1, track = 2)))
        assertEquals(0, nodesWithText("Disc 1"))
    }

    @Test
    fun discHeadersAreHiddenWhenDiscsAreMissing() {
        setContent(listOf(song("s-a", disc = null, track = 1), song("s-b", disc = null, track = 2)))
        assertEquals(0, nodesWithText("Disc 1"))
    }

    private fun setContent(
        songs: List<Song>,
        discs: List<Disc> = emptyList(),
    ) {
        composeRule.setContent {
            SubtracksTheme {
                AlbumDetailScreen(
                    album = album(),
                    songs = songs,
                    discs = discs,
                    coverArt = { _, _ -> null },
                    artwork = null,
                    onBack = {},
                    onSongClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { nodesWithText(songs.first().title) == 1 }
    }

    private fun nodesWithText(text: String) = composeRule.onAllNodesWithText(text, ignoreCase = true).fetchSemanticsNodes().size

    private fun song(
        id: String,
        disc: Long?,
        track: Long,
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = "al-1",
        artistId = "ar-1",
        title = "Song $id",
        album = "Album",
        artist = "Artist",
        duration = 200,
        track = track,
        disc = disc,
        starred = null,
        genre = null,
    )

    private fun disc(
        disc: Long,
        title: String,
    ) = Disc(sourceId = 1, albumId = "al-1", disc = disc, title = title)

    private fun album() =
        Album(
            sourceId = 1,
            id = "al-1",
            artistId = "ar-1",
            name = "Album",
            albumArtist = "Artist",
            created = 0,
            coverArt = null,
            genre = null,
            year = null,
            starred = null,
            songCount = 3,
        )
}
