package com.subtracks.ui.playback

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.CoverArtRef
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.QueueItem
import com.subtracks.ui.theme.SubtracksTheme
import com.subtracks.ui.theme.artworkColorsFromSeed
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NowPlayingTapsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val state =
        PlaybackState(
            item =
                QueueItem(
                    id = "s-eiirp",
                    title = "Everything In Its Right Place",
                    artist = "Radiohead",
                    album = "Kid A",
                    coverArtId = "art-al-kid-a",
                ),
            isPlaying = true,
            durationMs = 251_000,
            hasNext = true,
            hasPrevious = false,
        )

    private fun render(
        onAlbumClick: (() -> Unit)? = null,
        onArtistClick: (() -> Unit)? = null,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                NowPlayingScreen(
                    state = state,
                    positionMs = 62_000,
                    title = "Kid A",
                    coverArt = CoverArtRef("art-al-kid-a", "test:art-al-kid-a"),
                    artwork = artworkColorsFromSeed(android.graphics.Color.rgb(120, 80, 200)),
                    onBack = {},
                    onQueue = {},
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick,
                    onSeek = {},
                )
            }
        }
    }

    @Test
    fun tappingTheCoverArtOpensTheAlbum() {
        var opened = false
        render(onAlbumClick = { opened = true })

        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).performClick()

        assertTrue("the cover art should open the album", opened)
    }

    @Test
    fun tappingTheArtistNameOpensTheArtist() {
        var opened = false
        render(onArtistClick = { opened = true })

        composeRule.onNodeWithText("Radiohead").performClick()

        assertTrue("the artist name should open the artist", opened)
    }

    @Test
    fun theArtistTargetIsAtLeastTheMinimumTouchSize() {
        render(onArtistClick = {})

        // The block is the title line (32dp) plus the gap (6dp) plus the artist line
        // (20dp); it must not absorb the slider's own top padding.
        composeRule.onNodeWithText("Radiohead").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText("Radiohead").assertHeightIsEqualTo(58.dp)
        composeRule.onNodeWithText("Radiohead").assertWidthIsAtLeast(48.dp)
    }

    @Test
    fun theArtistTargetHugsTheTextRatherThanTheRow() {
        render(onArtistClick = {})

        val target =
            composeRule
                .onNodeWithText("Radiohead")
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
    fun theTapTargetsStayInertWithoutADestination() {
        render()

        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).assertIsNotEnabled()
        composeRule.onNodeWithText("Radiohead").assertIsNotEnabled()
    }
}
