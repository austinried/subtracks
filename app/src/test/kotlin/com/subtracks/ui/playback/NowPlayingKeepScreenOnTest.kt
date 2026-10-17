package com.subtracks.ui.playback

import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.CoverArtRef
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.QueueItem
import com.subtracks.ui.theme.SubtracksTheme
import com.subtracks.ui.theme.artworkColorsFromSeed
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NowPlayingKeepScreenOnTest {
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

    @Test
    fun theScreenOnFlagTracksTheSetting() {
        var keepScreenOn by mutableStateOf(true)
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
                    onSeek = {},
                    keepScreenOn = keepScreenOn,
                )
            }
        }
        composeRule.waitForIdle()
        assertNotNull("now playing should hold the screen on", viewHoldingTheScreenOn())

        composeRule.runOnIdle { keepScreenOn = false }
        composeRule.waitForIdle()
        assertNull("clearing the setting should release the screen-on flag", viewHoldingTheScreenOn())
    }

    private fun viewHoldingTheScreenOn(): View? = findKeepingScreenOn(composeRule.activity.window.decorView)

    private fun findKeepingScreenOn(view: View): View? {
        if (view.keepScreenOn) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findKeepingScreenOn(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }
}
