package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AlbumDetailTapsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val album =
        Album(
            sourceId = 1,
            id = "al-kid-a",
            artistId = "ar-radiohead",
            name = "Kid A",
            albumArtist = "Radiohead",
            created = 0,
            coverArt = "art-al-kid-a",
            genre = null,
            year = 2000,
            starred = null,
            songCount = 1,
        )

    private fun render(onArtistClick: (() -> Unit)? = null) {
        composeRule.setContent {
            SubtracksTheme {
                AlbumDetailScreen(
                    album = album,
                    songs = emptyList(),
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    artwork = null,
                    onBack = {},
                    onSongClick = {},
                    onArtistClick = onArtistClick,
                )
            }
        }
    }

    @Test
    fun tappingTheArtistOpensTheArtist() {
        var opened = false
        render(onArtistClick = { opened = true })

        composeRule.onNodeWithText("Radiohead \u00B7 2000").performClick()

        assertTrue("the artist name should open the artist", opened)
    }

    @Test
    fun theArtistTargetCoversTheLineAndItsGaps() {
        render(onArtistClick = {})

        composeRule.onNodeWithText("Radiohead \u00B7 2000").assertHeightIsAtLeast(32.dp)
    }

    @Test
    fun theArtistTargetHugsTheTextRatherThanTheRow() {
        render(onArtistClick = {})

        val target =
            composeRule
                .onNodeWithText("Radiohead \u00B7 2000")
                .fetchSemanticsNode()
                .boundsInRoot.width
        val screen =
            composeRule
                .onRoot()
                .fetchSemanticsNode()
                .boundsInRoot.width

        assertTrue("the target should not span the row", target < screen)
    }

    @Test
    fun theArtistTargetKeepsClearOfTheControls() {
        render(onArtistClick = {})

        val target = composeRule.onNodeWithText("Radiohead \u00B7 2000").fetchSemanticsNode().boundsInRoot
        val controls = composeRule.onNodeWithContentDescription("Download").fetchSemanticsNode().boundsInRoot
        val gap = controls.top - target.bottom

        assertTrue("expected a clear gap above the controls, was ${gap}px", gap >= with(composeRule.density) { 12.dp.roundToPx() })
    }

    @Test
    fun theAlbumTitleIsNotATapTarget() {
        render(onArtistClick = {})

        // The app bar repeats the album name, so match the hero title by having no click.
        composeRule.onAllNodesWithText("Kid A").filter(hasClickAction().not()).assertCountEquals(1)
    }
}
