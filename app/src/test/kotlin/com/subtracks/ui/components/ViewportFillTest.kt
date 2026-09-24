package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.ui.theme.SubtracksTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class ViewportFillTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun scrollingAShortListDoesNotGrowTheFill() {
        lateinit var state: LazyListState
        var fillHeight = 0
        composeRule.setContent {
            SubtracksTheme {
                state = rememberLazyListState()
                val fill = rememberViewportFill(state)
                LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
                    items(3) { Text("item $it", modifier = Modifier.height(56.dp)) }
                    item { Spacer(Modifier.height(fill).onSizeChanged { fillHeight = it.height }) }
                }
            }
        }
        composeRule.waitForIdle()

        val initial = fillHeight
        repeat(4) {
            composeRule.runOnIdle { runBlocking { state.scrollBy(4f) } }
            composeRule.waitForIdle()
        }

        assertEquals(initial, fillHeight)
    }
}
