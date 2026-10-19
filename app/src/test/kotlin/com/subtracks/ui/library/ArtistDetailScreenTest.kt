package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistAlbums
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtistDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun render(
        artist: Artist?,
        artistName: String,
        albums: ArtistAlbums = ArtistAlbums(),
        albumsLoaded: Boolean = true,
        bio: String? = null,
        bioLoading: Boolean = false,
        bioUnavailable: Boolean = false,
        bioPending: Boolean = false,
    ) {
        composeRule.setContent {
            var expanded by remember { mutableStateOf(false) }
            SubtracksTheme {
                ArtistDetailScreen(
                    artist = artist,
                    artistName = artistName,
                    albums = albums,
                    albumsLoaded = albumsLoaded,
                    bio = bio,
                    bioLoading = bioLoading,
                    bioUnavailable = bioUnavailable,
                    bioPending = bioPending,
                    bioExpanded = expanded,
                    art = null,
                    coverArt = { _, _ -> null },
                    onBack = {},
                    onAlbumClick = {},
                    onExpandBio = { expanded = true },
                    onCollapseBio = { expanded = false },
                )
            }
        }
    }

    @Test
    fun aCreditedTrackArtistShowsItsDerivedNameAndIsLabelledArtist() {
        render(
            artist = null,
            artistName = "Guest",
            albums = ArtistAlbums(appearances = listOf(album("al-1", "Guest Album"))),
        )

        composeRule.onAllNodesWithText("Guest")[0].assertExists()
        composeRule.onNodeWithText("Artist · 1 album").assertExists()
        composeRule.onAllNodesWithContentDescription("More options").assertCountEquals(0)
    }

    @Test
    fun anAlbumArtistIsLabelledAlbumArtist() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            albums = ArtistAlbums(albumCredits = listOf(album("al-1", "Kid A"))),
        )

        composeRule.onNodeWithText("Album artist · 1 album").assertExists()
        composeRule.onNodeWithContentDescription("More options").assertExists()
    }

    @Test
    fun mixedCreditsShowAlbumCreditsAndAppearancesInSeparateSections() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 2, starred = null),
            artistName = "Radiohead",
            albums =
                ArtistAlbums(
                    albumCredits = listOf(album("al-1", "Kid A")),
                    appearances = listOf(album("al-2", "Guest Album")),
                ),
        )

        composeRule.onNodeWithText("Album artist · 2 albums").assertExists()

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("ALBUMS"))
        composeRule.onNodeWithText("ALBUMS").assertExists()
        composeRule.onNodeWithText("Kid A").assertExists()

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("APPEARS ON"))
        composeRule.onNodeWithText("APPEARS ON").assertExists()
        composeRule.onNodeWithText("Guest Album").assertExists()
    }

    @Test
    fun theTypeAndCountStayBlankUntilTheAlbumsLoad() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            albumsLoaded = false,
        )

        composeRule.onNodeWithText("Artist · 0 albums").assertDoesNotExist()
        composeRule.onNodeWithText("Album artist · 0 albums").assertDoesNotExist()
    }

    @Test
    fun aStoredBioShowsImmediatelyEvenWhileItRefreshes() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bio = "A stored biography.",
            bioLoading = true,
        )

        composeRule.onNodeWithText("A stored biography.").assertExists()
        composeRule.onNodeWithText("Loading artist bio…").assertDoesNotExist()
    }

    @Test
    fun aMissingBioShowsTheLoadingStateFirst() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bioLoading = true,
        )

        composeRule.onNodeWithText("Loading artist bio…").assertExists()
        composeRule.onNodeWithText("No artist bio available.").assertDoesNotExist()
    }

    @Test
    fun theBioAreaWaitsForTheStoredValueBeforeChoosingAText() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bioLoading = true,
            bioPending = true,
        )

        composeRule.onNodeWithText("Loading artist bio…").assertDoesNotExist()
        composeRule.onNodeWithText("No artist bio available.").assertDoesNotExist()
        composeRule.onNodeWithText("Artist bio unavailable.").assertDoesNotExist()
    }

    @Test
    fun aFailedRefreshIsNotMistakenForAnArtistWithoutABio() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bioUnavailable = true,
        )

        composeRule.onNodeWithText("Artist bio unavailable.").assertExists()
        composeRule.onNodeWithText("No artist bio available.").assertDoesNotExist()
    }

    @Test
    fun aMissingBioShowsTheEmptyStateAfterTheRefresh() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
        )

        composeRule.onNodeWithText("No artist bio available.").assertExists()
        composeRule.onNodeWithText("Loading artist bio…").assertDoesNotExist()
    }

    @Test
    fun aShortBioDoesNotOfferMore() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bio = "A short biography.",
        )

        composeRule.onNodeWithTag(ARTIST_BIO_MORE_TAG).assertDoesNotExist()
    }

    @Test
    fun aLongBioExpandsWhenMoreIsTapped() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            bio =
                "Radiohead are an English rock band formed in Abingdon, Oxfordshire, in 1985. " +
                    "The band consists of Thom Yorke, Jonny Greenwood, Colin Greenwood, Ed O'Brien, " +
                    "and Philip Selway.",
        )

        composeRule.onNodeWithTag(ARTIST_BIO_MORE_TAG).performScrollTo().assertExists()

        composeRule.onNodeWithTag(ARTIST_BIO_MORE_TAG).performClick()

        composeRule.onNodeWithTag(ARTIST_BIO_MORE_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(ARTIST_BIO_LESS_TAG).performScrollTo().assertExists()

        composeRule.onNodeWithTag(ARTIST_BIO_LESS_TAG).performClick()

        composeRule.onNodeWithTag(ARTIST_BIO_LESS_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(ARTIST_BIO_MORE_TAG).performScrollTo().assertExists()
    }

    @Test
    fun theCollapsedSecondLineContinuesWhereTheFirstLineEnds() {
        val bio =
            "Radiohead are an English rock band formed in Abingdon, Oxfordshire, in 1985. " +
                "The band consists of Thom Yorke, Jonny Greenwood, Colin Greenwood, Ed O'Brien, " +
                "and Philip Selway."
        var result: BioLines? = null
        composeRule.setContent {
            val measurer = rememberTextMeasurer()
            result = splitBioCollapsed(measurer, bio, TextStyle(fontSize = 14.sp), 400, 0, 0, "More")
        }
        composeRule.waitForIdle()

        val lines = requireNotNull(result)
        assertTrue(lines.more)
        val firstWords =
            lines.lines[0]
                .text
                .split(' ')
                .filter { it.isNotEmpty() }
        val next = bio.split(' ').filter { it.isNotEmpty() }[firstWords.size]
        assertTrue(
            "expected the second line to continue with $next, was ${lines.lines[1].text}",
            lines.lines[1].text.startsWith(next),
        )
    }

    @Test
    fun theCollapsedPreviewCanShowThreeLines() {
        val bio = List(6) { "This is a sufficiently long biography sentence." }.joinToString(" ")
        var result: BioLines? = null
        composeRule.setContent {
            val measurer = rememberTextMeasurer()
            result =
                splitBioCollapsed(
                    measurer,
                    bio,
                    TextStyle(fontSize = 14.sp),
                    400,
                    0,
                    0,
                    "More",
                    maxLines = 3,
                )
        }
        composeRule.waitForIdle()

        val lines = requireNotNull(result)
        assertEquals(3, lines.lines.size)
        assertTrue(lines.more)
        assertTrue(
            lines.lines
                .last()
                .text
                .endsWith("More"),
        )
    }

    @Test
    fun anOversizedFirstWordIsStillShown() {
        var result: BioLines? = null
        composeRule.setContent {
            val measurer = rememberTextMeasurer()
            result =
                splitBioCollapsed(
                    measurer,
                    "Supercalifragilisticexpialidocious and the rest",
                    TextStyle(fontSize = 14.sp),
                    20,
                    0,
                    0,
                    "More",
                )
        }
        composeRule.waitForIdle()

        val lines = requireNotNull(result)
        assertTrue(lines.more)
        assertTrue(
            lines.lines
                .first()
                .text
                .isNotBlank(),
        )
    }

    @Test
    fun sourceLineAndParagraphBreaksArePreserved() {
        var lineBreak: List<String>? = null
        var paragraphBreak: List<String>? = null
        composeRule.setContent {
            val measurer = rememberTextMeasurer()
            val style = TextStyle(fontSize = 14.sp)
            lineBreak = splitBio(measurer, "First line.\nSecond line.", style, 400, 0, 2)
            paragraphBreak = splitBio(measurer, "First paragraph.\n\nSecond paragraph.", style, 400, 0, 2)
        }
        composeRule.waitForIdle()

        assertEquals(listOf("First line.", "Second line."), lineBreak)
        assertEquals(listOf("First paragraph.", "", "Second paragraph."), paragraphBreak)
    }

    private fun album(
        id: String,
        name: String,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Radiohead",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )
}
