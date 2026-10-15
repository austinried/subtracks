package com.subtracks.playback

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class KnownDurationPlayerTest {
    private fun player(
        declaredDurationMs: Long?,
        live: Boolean = true,
    ): Player =
        object : SimpleBasePlayer(Looper.getMainLooper()) {
            override fun getState(): State {
                val metadata =
                    MediaMetadata
                        .Builder()
                        .setTitle("Track")
                        .setDurationMs(declaredDurationMs)
                        .build()
                val item =
                    MediaItemData
                        .Builder("track")
                        .setMediaItem(
                            MediaItem
                                .Builder()
                                .setMediaId("s1")
                                .setMediaMetadata(metadata)
                                .build(),
                        ).setDurationUs(PLAYER_DURATION_US)
                        .setLiveConfiguration(if (live) MediaItem.LiveConfiguration.UNSET else null)
                        .build()
                return State
                    .Builder()
                    .setPlaylist(listOf(item))
                    .build()
            }
        }

    @Test
    fun aLiveTrackWithADeclaredDurationIsReportedFinite() {
        val player = player(declaredDurationMs = 100_000L)

        assertFalse(KnownDurationPlayer(player).isCurrentMediaItemLive())
    }

    @Test
    fun aLiveTrackWithoutADeclaredDurationStaysLive() {
        val player = player(declaredDurationMs = null)

        assertTrue(KnownDurationPlayer(player).isCurrentMediaItemLive())
    }

    @Test
    fun aFiniteTrackWithADeclaredDurationStaysFinite() {
        val player = player(declaredDurationMs = 100_000L, live = false)

        assertFalse(KnownDurationPlayer(player).isCurrentMediaItemLive())
    }

    @Test
    fun theWrappedDurationIsThePlayersNotTheMetadata() {
        val player = player(declaredDurationMs = 100_000L)

        assertEquals(PLAYER_DURATION_MS, KnownDurationPlayer(player).getDuration())
    }

    private companion object {
        const val PLAYER_DURATION_MS = 200_000L
        const val PLAYER_DURATION_US = PLAYER_DURATION_MS * 1000
    }
}
