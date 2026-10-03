package com.subtracks.ui.playback

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.CoverArtRef
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.QueueItem
import com.subtracks.ui.theme.SubtracksTheme
import com.subtracks.ui.theme.artworkColorsFromSeed
import org.junit.Assert.assertEquals
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
        onNext: () -> Unit = {},
        onPrevious: () -> Unit = {},
        onSkipPrevious: () -> Unit = onPrevious,
        previousArt: NowPlayingArt? = null,
        nextArt: NowPlayingArt? = null,
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
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSkipPrevious = onSkipPrevious,
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick,
                    onSeek = {},
                    previousArt = previousArt,
                    nextArt = nextArt,
                )
            }
        }
    }

    private fun adjacentArt(id: String) =
        NowPlayingArt(
            id = id,
            ref = CoverArtRef(id, "test:$id"),
            thumbnailRef = null,
            name = id,
        )

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

    @Test
    fun swipingLeftOnTheCoverSkipsToTheNextTrack() {
        var next = false
        var previous = false
        render(onNext = { next = true }, onPrevious = { previous = true })

        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).performTouchInput { swipeLeft() }

        assertTrue("a left swipe should skip to the next track", next)
        assertTrue("a left swipe should not go to the previous track", !previous)
    }

    @Test
    fun swipingRightOnTheCoverGoesToThePreviousTrack() {
        var next = false
        var previous = false
        var skipPrevious = false
        render(
            onNext = { next = true },
            onPrevious = { previous = true },
            onSkipPrevious = { skipPrevious = true },
        )

        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).performTouchInput { swipeRight() }

        assertTrue("a right swipe should skip to the previous track", skipPrevious)
        assertTrue("a right swipe should not skip to the next track", !next)
        assertTrue("a right swipe should not use the restart-on-previous button", !previous)
    }

    @Test
    fun theStripHandsBackOnlyWhenTheLivePropsAgree() {
        val before = adjacentArt("s0")
        val current = adjacentArt("s1")
        val next = adjacentArt("s2")
        val after = adjacentArt("s3")
        val frozenNext = listOf(current, next, null)
        val frozenPrevious = listOf(null, before, current)

        assertTrue(!handBackReady(frozenNext, itemId = "s1", previousArt = before, nextArt = after))
        assertTrue(!handBackReady(frozenNext, itemId = "s2", previousArt = before, nextArt = after))
        assertTrue(handBackReady(frozenNext, itemId = "s2", previousArt = current, nextArt = after))
        assertTrue(handBackReady(frozenPrevious, itemId = "s0", previousArt = null, nextArt = current))
        assertTrue(!handBackReady(frozenPrevious, itemId = "s1", previousArt = null, nextArt = current))
    }

    @Test
    fun theSwipeActionCommitsWhenANeighbourExists() {
        assertEquals(
            SwipeAction.CommitNext,
            swipeAction(offsetX = -300f, velocity = 0f, width = 1000f, canGoNext = true, canGoPrevious = false),
        )
        assertEquals(
            SwipeAction.CommitPrevious,
            swipeAction(offsetX = 300f, velocity = 0f, width = 1000f, canGoNext = false, canGoPrevious = true),
        )
    }

    @Test
    fun theSwipeActionFallsBackWhenTheNeighbourIsMissing() {
        assertEquals(
            SwipeAction.Next,
            swipeAction(offsetX = -300f, velocity = 0f, width = 1000f, canGoNext = false, canGoPrevious = false),
        )
        assertEquals(
            SwipeAction.Previous,
            swipeAction(offsetX = 300f, velocity = 0f, width = 1000f, canGoNext = false, canGoPrevious = false),
        )
    }

    @Test
    fun theSwipeActionIgnoresShortAndUnmeasuredSwipes() {
        assertEquals(
            SwipeAction.None,
            swipeAction(offsetX = -50f, velocity = 0f, width = 1000f, canGoNext = true, canGoPrevious = true),
        )
        assertEquals(
            SwipeAction.None,
            swipeAction(offsetX = -300f, velocity = 0f, width = 0f, canGoNext = true, canGoPrevious = true),
        )
    }

    @Test
    fun theSwipeActionPrefersTheDraggedDirectionOverAnOppositeFling() {
        assertEquals(
            SwipeAction.CommitPrevious,
            swipeAction(offsetX = 300f, velocity = -2_000f, width = 1_000f, canGoNext = true, canGoPrevious = true),
        )
        assertEquals(
            SwipeAction.CommitNext,
            swipeAction(offsetX = -300f, velocity = 2_000f, width = 1_000f, canGoNext = true, canGoPrevious = true),
        )
    }

    @Test
    fun swipingLeftWithANeighbourRotatesTheStrip() {
        render(nextArt = adjacentArt("s-next"))

        composeRule.onNodeWithTag(NOW_PLAYING_NEXT_TAG, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(NOW_PLAYING_NEXT_TAG, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag(NOW_PLAYING_PREVIOUS_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun rotatingTheStripMovesTheNeighbourIntoTheCentre() {
        val a = adjacentArt("a")
        val b = adjacentArt("b")
        val c = adjacentArt("c")
        val base = listOf(a, b, c)

        assertEquals(listOf(b, c, null), rotateStrip(base, 1))
        assertEquals(listOf(null, a, b), rotateStrip(base, -1))
    }

    @Test
    fun theAdjacentArtRendersBesideTheCurrentArt() {
        render(
            previousArt = adjacentArt("s-previous"),
            nextArt = adjacentArt("s-next"),
        )

        composeRule.onNodeWithTag(NOW_PLAYING_PREVIOUS_TAG, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag(NOW_PLAYING_NEXT_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun theAdjacentArtIsAbsentWithoutNeighbours() {
        render()

        composeRule.onNodeWithTag(NOW_PLAYING_PREVIOUS_TAG, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithTag(NOW_PLAYING_NEXT_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun aShortSlowDragOnTheCoverDoesNotSkip() {
        var next = false
        var previous = false
        render(onNext = { next = true }, onPrevious = { previous = true })

        val cover = composeRule.onNodeWithTag(NOW_PLAYING_COVER_TAG)
        val width =
            cover
                .fetchSemanticsNode()
                .size.width
                .toFloat()
        cover.performTouchInput {
            swipe(
                start = Offset(width * 0.9f, centerY),
                end = Offset(width * 0.7f, centerY),
                durationMillis = 600,
            )
        }

        assertTrue("a drag below the threshold should not skip", !next)
        assertTrue("a drag below the threshold should not go to the previous track", !previous)
    }
}
