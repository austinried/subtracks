package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LibraryInfoDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun anAlbumShowsEveryArtistAndBothSortNames() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumInfoDialog(
                    album =
                        Album(
                            sourceId = 1,
                            id = "al1",
                            artistId = "ar1",
                            name = "Album",
                            albumArtist = "A",
                            created = 0,
                            coverArt = null,
                            genre = "Rock",
                            year = 1999,
                            starred = null,
                            songCount = 12,
                            artists = listOf(ArtistRef("ar1", "A"), ArtistRef("ar2", "B")),
                            sortName = "Album, The",
                            albumArtistSort = "A, The",
                        ),
                    download = null,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("A · B").assertIsDisplayed()
        composeRule.onNodeWithText("Album, The").assertIsDisplayed()
        composeRule.onNodeWithText("A, The").assertIsDisplayed()
        composeRule.onNodeWithText("1999").assertIsDisplayed()

        val label = composeRule.onNodeWithText("Album artist sort").fetchSemanticsNode().boundsInRoot
        val value = composeRule.onNodeWithText("A, The").fetchSemanticsNode().boundsInRoot
        assertTrue("the value must be separated from its label", value.left > label.right)
    }

    @Test
    fun anArtistShowsItsSortNameAndCounts() {
        composeRule.setContent {
            SubtracksTheme {
                ArtistInfoDialog(
                    artist =
                        Artist(
                            sourceId = 1,
                            id = "ar1",
                            name = "The Artist",
                            albumCount = 4,
                            starred = null,
                            songCount = 40,
                            sortName = "Artist, The",
                        ),
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Artist, The").assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithText("40").assertIsDisplayed()
    }

    @Test
    fun anArtistHidesASortNameThatMatchesItsName() {
        composeRule.setContent {
            SubtracksTheme {
                ArtistInfoDialog(
                    artist =
                        Artist(
                            sourceId = 1,
                            id = "ar1",
                            name = "The Artist",
                            albumCount = 1,
                            starred = null,
                            sortName = "the artist",
                        ),
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Sort name").assertDoesNotExist()
    }

    @Test
    fun anAlbumHidesSortNamesThatMatchTheRegularNames() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumInfoDialog(
                    album =
                        Album(
                            sourceId = 1,
                            id = "al1",
                            artistId = "ar1",
                            name = "Album",
                            albumArtist = "Artist",
                            created = 0,
                            coverArt = null,
                            genre = null,
                            year = null,
                            starred = null,
                            songCount = 1,
                            sortName = "album",
                            albumArtistSort = "artist",
                        ),
                    download = null,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Sort title").assertDoesNotExist()
        composeRule.onNodeWithText("Album artist sort").assertDoesNotExist()
    }

    @Test
    fun anAlbumsDatesAreListedTogether() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumInfoDialog(
                    album =
                        Album(
                            sourceId = 1,
                            id = "al1",
                            artistId = "ar1",
                            name = "Album",
                            albumArtist = "Artist",
                            created = 86400,
                            coverArt = null,
                            genre = null,
                            year = null,
                            starred = null,
                            songCount = 1,
                            playCount = 3,
                            played = 172800,
                        ),
                    download = null,
                    onDismiss = {},
                )
            }
        }

        val added = composeRule.onNodeWithText("Added").fetchSemanticsNode().boundsInRoot
        val lastPlayed = composeRule.onNodeWithText("Last played").fetchSemanticsNode().boundsInRoot
        val plays = composeRule.onNodeWithText("Plays").fetchSemanticsNode().boundsInRoot
        assertTrue("last played must follow added", lastPlayed.top >= added.bottom)
        assertTrue("plays must not sit between the dates", plays.top >= lastPlayed.bottom)
        assertEquals(
            "no row may sit between added and last played",
            lastPlayed.top - added.top,
            plays.top - lastPlayed.top,
            0.5f,
        )
    }

    @Test
    fun aPlaylistShowsOwnerSharingLengthAndDates() {
        composeRule.setContent {
            SubtracksTheme {
                PlaylistInfoDialog(
                    playlist =
                        Playlist(
                            sourceId = 1,
                            id = "pl1",
                            name = "Mix",
                            comment = null,
                            coverArt = null,
                            songCount = 12,
                            created = 1600000000,
                            changed = 1700000000,
                            duration = 3725,
                            owner = "austin",
                            shared = true,
                        ),
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Owner").assertIsDisplayed()
        composeRule.onNodeWithText("austin").assertIsDisplayed()
        composeRule.onNodeWithText("Shared").assertIsDisplayed()
        composeRule.onNodeWithText("Yes").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()
        composeRule.onNodeWithText("1:02:05").assertIsDisplayed()
        composeRule.onNodeWithText("Created").assertIsDisplayed()
        composeRule.onNodeWithText("Updated").assertIsDisplayed()
    }

    @Test
    fun aSingleArtistTrackUsesSingularLabels() {
        composeRule.setContent {
            SubtracksTheme {
                SongInfoDialog(
                    song = song(artists = listOf(ArtistRef("ar1", "Alpha")), artistSort = "Beta"),
                    download = null,
                    localFile = null,
                    streamEncoding = null,
                    onDismiss = {},
                    genres = emptyList(),
                )
            }
        }

        composeRule.onNodeWithText("Artist").assertIsDisplayed()
        composeRule.onNodeWithText("Artist sort").assertIsDisplayed()
        composeRule.onNodeWithText("Artists").assertDoesNotExist()
        composeRule.onNodeWithText("Artists sort").assertDoesNotExist()
    }

    @Test
    fun duplicateAndBlankArtistRefsStaySingular() {
        composeRule.setContent {
            SubtracksTheme {
                SongInfoDialog(
                    song =
                        song(
                            artists = listOf(ArtistRef("ar1", "Alpha"), ArtistRef("ar2", "Alpha"), ArtistRef("ar3", "")),
                            artistSort = "Beta",
                        ),
                    download = null,
                    localFile = null,
                    streamEncoding = null,
                    onDismiss = {},
                    genres = emptyList(),
                )
            }
        }

        composeRule.onNodeWithText("Artist").assertIsDisplayed()
        composeRule.onNodeWithText("Artists").assertDoesNotExist()
    }

    @Test
    fun aMixedArtistTrackUsesPluralLabelsForBothRows() {
        composeRule.setContent {
            SubtracksTheme {
                SongInfoDialog(
                    song =
                        song(
                            artists = listOf(ArtistRef("ar1", "Artist A"), ArtistRef("ar2", "Artist B")),
                            artistSort = "Alpha",
                        ),
                    download = null,
                    localFile = null,
                    streamEncoding = null,
                    onDismiss = {},
                    genres = emptyList(),
                )
            }
        }

        composeRule.onNodeWithText("Artists").assertIsDisplayed()
        composeRule.onNodeWithText("Artists sort").assertIsDisplayed()
        composeRule.onNodeWithText("Artist").assertDoesNotExist()
        composeRule.onNodeWithText("Artist sort").assertDoesNotExist()
        composeRule.onNodeWithText("Alpha").assertIsDisplayed()
    }

    @Test
    fun aTrackHidesATitleAndArtistSortThatMatchTheRegularNames() {
        composeRule.setContent {
            SubtracksTheme {
                SongInfoDialog(
                    song = song(artists = listOf(ArtistRef("ar1", "Artist")), sortName = "song", artistSort = "artist"),
                    download = null,
                    localFile = null,
                    streamEncoding = null,
                    onDismiss = {},
                    genres = emptyList(),
                )
            }
        }

        composeRule.onNodeWithText("Sort title").assertDoesNotExist()
        composeRule.onNodeWithText("Artist sort").assertDoesNotExist()
    }

    private fun song(
        artists: List<ArtistRef>,
        sortName: String = "Song",
        artistSort: String = "Artist",
    ) = Song(
        sourceId = 1,
        id = "s1",
        albumId = "al1",
        artistId = artists.first().id,
        title = "Song",
        album = "Album",
        artist = artists.first().name,
        duration = 200,
        track = 1,
        disc = 1,
        starred = null,
        artists = artists,
        sortName = sortName,
        artistSort = artistSort,
    )
}
