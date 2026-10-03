package com.subtracks.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class HomeRevealScrollTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun aNewFrontItemAtTheStartScrollsBack() {
        val labels = mutableStateOf(listOf("a", "b", "c", "d", "e"))
        lateinit var state: LazyListState
        composeRule.setContent {
            SubtracksTheme {
                state = rememberLazyListState()
                RevealNewFrontItem(labels.value, state) { it }
                LazyRow(state = state, modifier = Modifier.fillMaxSize()) {
                    items(labels.value, key = { it }) { label ->
                        Text(label, modifier = Modifier.width(200.dp).height(80.dp))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, state.firstVisibleItemIndex)

        composeRule.runOnIdle { labels.value = listOf("z") + labels.value }

        composeRule.waitForIdle()
        assertEquals(0, state.firstVisibleItemIndex)
    }
}
