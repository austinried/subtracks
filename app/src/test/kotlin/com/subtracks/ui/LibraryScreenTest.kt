package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.SongListItem
import com.subtracks.ui.library.ALBUM_COVER_TAG
import com.subtracks.ui.library.LibraryScreen
import com.subtracks.ui.library.LibraryTab
import com.subtracks.ui.library.LibraryTabs
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
    fun pullingDownRefreshesAndSettingsOpens() {
        var synced = false
        var settingsOpened = false
        composeRule.setContent {
            SubtracksTheme {
                LibraryScreen(
                    selectedTab = LibraryTab.Albums,
                    onTabSelected = {},
                    albums = remember { flowOf(PagingData.from(albums())) }.collectAsLazyPagingItems(),
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

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(ALBUM_COVER_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().performTouchInput { swipeDown(durationMillis = 600) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue("pulling down should trigger a sync", synced)
        assertTrue("the settings control should open settings", settingsOpened)
    }

    @Test
    fun anEmptyTabOffersSync() {
        var synced = false
        composeRule.setContent {
            SubtracksTheme {
                LibraryScreen(
                    selectedTab = LibraryTab.Albums,
                    onTabSelected = {},
                    albums = remember { flowOf(PagingData.empty<Album>(settledLoadStates)) }.collectAsLazyPagingItems(),
                    artists = remember { flowOf(PagingData.empty<Artist>()) }.collectAsLazyPagingItems(),
                    songs = remember { flowOf(PagingData.empty<SongListItem>()) }.collectAsLazyPagingItems(),
                    playlists = remember { flowOf(PagingData.empty<Playlist>()) }.collectAsLazyPagingItems(),
                    coverArt = { _, _ -> null },
                    onAlbumClick = {},
                    onArtistClick = {},
                    onPlaylistClick = {},
                    onSongClick = {},
                    onSync = { synced = true },
                    onOpenSettings = {},
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Sync").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Sync").performClick()

        assertTrue("an empty library should offer a sync", synced)
    }

    @Test
    fun theTabRowReflowsInOneDirectionDuringASwitch() {
        val pagerState = PagerState(currentPage = 1, pageCount = { LibraryTab.entries.size })
        composeRule.setContent {
            SubtracksTheme {
                LibraryTabs(
                    pagerState = pagerState,
                    onTabSelected = {},
                    onOpenSettings = {},
                    artwork = null,
                )
            }
        }
        composeRule.waitForIdle()

        val samples =
            (0..10).map { step ->
                composeRule.runOnIdle { pagerState.requestScrollToPage(1, step * 0.05f) }
                composeRule.waitForIdle()
                composeRule
                    .onNodeWithContentDescription(LibraryTab.Songs.label)
                    .fetchSemanticsNode()
                    .boundsInRoot.left
            }

        val monotonic =
            samples.zipWithNext().all { (a, b) -> b >= a } ||
                samples.zipWithNext().all { (a, b) -> b <= a }
        assertTrue("the tab row should reflow in one direction, but the icons moved $samples", monotonic)
    }

    private val settledLoadStates =
        LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )

    private fun albums() =
        (1..60).map { index ->
            Album(
                sourceId = 1,
                id = "al-$index",
                artistId = "ar-1",
                name = "Album $index",
                albumArtist = "Artist",
                created = 0,
                coverArt = null,
                genre = null,
                year = null,
                starred = null,
                songCount = 1,
                frequentRank = null,
                recentRank = null,
            )
        }
}
