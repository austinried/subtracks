package com.subtracks.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Render smoke test: asserts the awaited node exists and writes a PNG for local review; it does not verify pixels. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class HomeScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun home() {
        composeRule.setContent {
            SubtracksTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        feed = feed(),
                        coverArt = { _, _ -> null },
                        playingSongId = null,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Recently played").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/home.png")
    }

    private fun feed() =
        HomeFeed(
            recentlyPlayedAlbums = albums("Recent"),
            recentlyPlayedArtists = artists("Recent"),
            mostPlayedAlbums = albums("Frequent"),
            mostPlayedArtists = artists("Frequent"),
            genres = listOf("Rock", "Jazz", "Classical", "Electronic", "Pop", "Folk", "Blues"),
            decades = listOf(1960L, 1970L, 1980L, 1990L, 2000L, 2010L),
            recentlyStarredSongs = songs(),
            recentlyAddedAlbums = albums("Added"),
            rediscoverAlbums = albums("Repeat"),
        )

    private fun albums(prefix: String) =
        (1..6).map { index ->
            Album(
                sourceId = 1,
                id = "$prefix-$index",
                artistId = "ar-$index",
                name = "$prefix Album $index",
                albumArtist = "Artist",
                created = 0,
                coverArt = null,
                genre = null,
                year = 2000,
                starred = null,
                songCount = 1,
            )
        }

    private fun artists(prefix: String) =
        (1..6).map { index ->
            Artist(
                sourceId = 1,
                id = "$prefix-ar-$index",
                name = "$prefix Artist $index",
                albumCount = 2,
                starred = null,
                coverArt = null,
            )
        }

    private fun songs() =
        (1..5).map { index ->
            AlbumSongItem(
                song =
                    Song(
                        sourceId = 1,
                        id = "song-$index",
                        albumId = "Recent-1",
                        artistId = "ar-1",
                        title = "Starred Song $index",
                        album = "Album",
                        artist = "Artist",
                        duration = 185,
                        track = index.toLong(),
                        disc = 1,
                        starred = 1000L - index,
                        genre = null,
                    ),
                coverArt = null,
            )
        }
}
