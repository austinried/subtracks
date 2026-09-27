package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HangingTitleTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var measurer: TextMeasurer? = null
    private var style: TextStyle = TextStyle.Default
    private var fullWidth = 0
    private var reserve = 0

    private fun setUpMeasurements() {
        composeRule.setContent {
            SubtracksTheme {
                measurer = rememberTextMeasurer()
                style = MaterialTheme.typography.headlineLarge
                val density = LocalDensity.current
                fullWidth = with(density) { 360.dp.roundToPx() }
                reserve = with(density) { 80.dp.roundToPx() }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun aShortNameStaysOnOneLine() {
        setUpMeasurements()

        assertEquals(listOf("Boards"), splitTitle(measurer!!, "Boards", style, fullWidth, reserve))
    }

    @Test
    fun theLastLineStopsBeforeTheReservedWidth() {
        setUpMeasurements()

        val lines = splitTitle(measurer!!, "Supercalifragilisticexpialidocious x", style, fullWidth, reserve)

        assertEquals(2, lines.size)
        assertEquals("x", lines.last())
        val lastWidth = measurer!!.measure(AnnotatedString(lines.last()), style, maxLines = 1).size.width
        assertTrue("the last line must stop before the reserved width", lastWidth <= fullWidth - reserve)
    }
}
