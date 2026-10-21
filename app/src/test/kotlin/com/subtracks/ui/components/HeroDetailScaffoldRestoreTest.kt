package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroDetailScaffoldRestoreTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun aRestoredScreenWaitsForItsRowsBeforeRestoringTheScrollPosition() {
        var visible by mutableStateOf(true)
        var rows by mutableIntStateOf(100)
        composeRule.setContent {
            SubtracksTheme {
                val holder = rememberSaveableStateHolder()
                if (visible) {
                    holder.SaveableStateProvider("detail") {
                        HeroDetailScaffold(
                            artwork = null,
                            title = "Album",
                            onBack = {},
                            header = { _, _ -> Box(Modifier.height(200.dp)) },
                            content = { rowModifier -> rowItems(rows, rowModifier) },
                            contentReady = rows > 0,
                        )
                    }
                }
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Item 20"))
        composeRule.onNodeWithText("Item 20").assertIsDisplayed()

        composeRule.runOnIdle { visible = false }
        composeRule.runOnIdle {
            rows = 0
            visible = true
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { rows = 100 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Item 20").assertIsDisplayed()
    }

    private fun LazyListScope.rowItems(
        count: Int,
        modifier: Modifier,
    ) {
        repeat(count) { index ->
            item(key = index) {
                Box(modifier.height(48.dp)) {
                    Text("Item $index")
                }
            }
        }
    }
}
