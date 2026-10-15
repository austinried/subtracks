package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
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
class AlbumsPlaceholderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun anUnloadedCellIsDrawnAsAPlaceholder() {
        composeRule.setContent {
            SubtracksTheme {
                val items =
                    remember { flowOf(PagingData.from(listOf(album(0)), placeholdersAfter = 2)) }
                        .collectAsLazyPagingItems()
                AlbumsContent(items, { _, _ -> null }, 0.dp, {})
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(ALBUM_PLACEHOLDER_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        assertEquals(2, composeRule.onAllNodesWithTag(ALBUM_PLACEHOLDER_TAG).fetchSemanticsNodes().size)
        assertEquals(1, composeRule.onAllNodesWithTag(ALBUM_COVER_TAG).fetchSemanticsNodes().size)
    }

    private fun album(index: Int) =
        Album(
            sourceId = 1,
            id = "al-" + index.toString().padStart(4, '0'),
            artistId = "ar",
            name = "Album " + index.toString().padStart(4, '0'),
            albumArtist = "Artist",
            created = 0,
            coverArt = null,
            genre = null,
            year = null,
            starred = null,
            songCount = 1,
        )
}
