package com.subtracks.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.prefs.StreamQuality
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
class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theKeepScreenOnRowTurnsTheSettingOn() {
        var requested: Boolean? = null
        renderSettings(keepScreenOn = false) { requested = it }

        composeRule.onNodeWithText("Keep screen on").performClick()
        composeRule.waitForIdle()

        assertEquals(true, requested)
    }

    @Test
    fun theKeepScreenOnRowTurnsTheSettingOff() {
        var requested: Boolean? = null
        renderSettings(keepScreenOn = true) { requested = it }

        composeRule.onNodeWithText("Keep screen on").performClick()
        composeRule.waitForIdle()

        assertEquals(false, requested)
    }

    @Test
    fun theKeepScreenOnSwitchTurnsTheSettingOn() {
        var requested: Boolean? = null
        renderSettings(keepScreenOn = false) { requested = it }

        composeRule
            .onNode(isToggleable() and hasAnySibling(hasText("Keep screen on")), useUnmergedTree = true)
            .performClick()
        composeRule.waitForIdle()

        assertEquals(true, requested)
    }

    private fun renderSettings(
        keepScreenOn: Boolean,
        onKeepScreenOnChange: (Boolean) -> Unit,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                SettingsScreen(
                    sources = emptyList(),
                    activeSourceId = null,
                    wifiQuality = StreamQuality(),
                    mobileQuality = StreamQuality(),
                    syncConcurrency = 1,
                    downloadQuality = StreamQuality(),
                    downloadOverMetered = false,
                    scrobbling = false,
                    keepScreenOn = keepScreenOn,
                    onSelectSource = {},
                    onEditServer = {},
                    onWifiQualityChange = {},
                    onMobileQualityChange = {},
                    onSyncConcurrencyChange = {},
                    onDownloadQualityChange = {},
                    onDownloadOverMeteredChange = {},
                    onScrobblingChange = {},
                    onKeepScreenOnChange = onKeepScreenOnChange,
                    onAddServer = {},
                    onOpenDownloads = {},
                    onOpenLicenses = {},
                    onShareLogs = {},
                    onBack = {},
                )
            }
        }
        awaitText("Add server")
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Keep screen on"))
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
