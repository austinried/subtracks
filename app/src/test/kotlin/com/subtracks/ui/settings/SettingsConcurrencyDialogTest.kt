package com.subtracks.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class SettingsConcurrencyDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theConcurrencyDialogListsEachPresetOnItsOwnRow() {
        composeRule.setContent {
            SubtracksTheme {
                SettingsScreen(
                    sources = emptyList(),
                    activeSourceId = null,
                    wifiQuality = StreamQuality(),
                    mobileQuality = StreamQuality(),
                    syncConcurrency = 5,
                    onSelectSource = {},
                    onDeleteSource = {},
                    onWifiQualityChange = {},
                    onMobileQualityChange = {},
                    onSyncConcurrencyChange = {},
                    onAddServer = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Sync concurrency").performClick()

        val rows =
            listOf("1 (sequential)", "2", "4", "8", "16").map {
                composeRule.onNodeWithText(it).fetchSemanticsNode().boundsInRoot
            }
        rows.zipWithNext().forEach { (above, below) ->
            assertTrue("concurrency options overlap: $above vs $below", below.top >= above.bottom)
        }
    }
}
