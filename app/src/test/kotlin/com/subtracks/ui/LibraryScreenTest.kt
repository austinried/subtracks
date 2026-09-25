package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.SongListItem
import com.subtracks.ui.library.LibraryScreen
import com.subtracks.ui.library.LibraryTab
import com.subtracks.ui.theme.SubtracksTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class LibraryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theSyncAndSettingsControlsAreReachable() {
        var synced = false
        var settingsOpened = false
        composeRule.setContent {
            SubtracksTheme {
                LibraryScreen(
                    selectedTab = LibraryTab.Albums,
                    onTabSelected = {},
                    albums = remember { flowOf(PagingData.empty<Album>()) }.collectAsLazyPagingItems(),
                    artists = remember { flowOf(PagingData.empty<Artist>()) }.collectAsLazyPagingItems(),
                    songs = remember { flowOf(PagingData.empty<SongListItem>()) }.collectAsLazyPagingItems(),
                    playlists = remember { flowOf(PagingData.empty<Playlist>()) }.collectAsLazyPagingItems(),
                    coverArt = { _, _ -> null },
                    onAlbumClick = {},
                    onArtistClick = {},
                    onPlaylistClick = {},
                    onSongClick = {},
                    onSync = { synced = true },
                    onOpenSettings = { settingsOpened = true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Sync").performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue("the sync control should trigger a sync", synced)
        assertTrue("the settings control should open settings", settingsOpened)
    }
}
