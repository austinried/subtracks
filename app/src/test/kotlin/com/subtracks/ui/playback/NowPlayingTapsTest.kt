package com.subtracks.ui.playback

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
    fun theTapTargetsStayInertWithoutADestination() {
        render()

        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).assertIsNotEnabled()
        composeRule.onNodeWithText("Radiohead").assertIsNotEnabled()
    }
}
