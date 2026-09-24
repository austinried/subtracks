package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.StarredFilter
import com.subtracks.ui.library.ALBUM_COVER_TAG
import com.subtracks.ui.library.LibraryScreen
import com.subtracks.ui.library.LibraryTab
import com.subtracks.ui.library.sortOptionsFor
import com.subtracks.ui.theme.SubtracksTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class LibraryOptionsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theOptionsSheetAppliesSortFilterAndClear() {
        var query by mutableStateOf(ListQuery("Name"))
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
                    onSync = {},
                    onOpenSettings = {},
                    listQuery = query,
                    sortOptions = sortOptionsFor(LibraryTab.Albums),
                    starredSupported = true,
                    onSortChange = { query = query.copy(sort = it) },
                    onCycleStarred = {
                        query =
                            query.copy(
                                starred =
                                    when (query.starred) {
                                        StarredFilter.Any -> StarredFilter.Starred
                                        StarredFilter.Starred -> StarredFilter.NotStarred
                                        StarredFilter.NotStarred -> StarredFilter.Any
                                    },
                            )
                    },
                    onClearFilters = { query = query.copy(starred = StarredFilter.Any) },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(ALBUM_COVER_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("List options").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Added").performClick()
        composeRule.waitForIdle()
        assertEquals("Added", query.sort)

        composeRule.onAllNodesWithText("Starred")[1].performClick()
        composeRule.waitForIdle()
        assertEquals(StarredFilter.Starred, query.starred)

        composeRule.onNodeWithText("Clear filters").performClick()
        composeRule.waitForIdle()
        assertEquals(StarredFilter.Any, query.starred)
    }

    private fun albums() =
        (1..3).map { index ->
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
