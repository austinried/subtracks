package com.subtracks.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.mikepenz.aboutlibraries.Libs
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Render smoke test: asserts the awaited node exists and writes a PNG for local review; it does not verify pixels. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class LicensesScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun licenses() {
        val libs = Libs.Builder().withJson(fixture).build()
        composeRule.setContent {
            SubtracksTheme {
                LicensesScreen(libs = libs, onBack = {})
            }
        }
        awaitText("OkHttp")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/licenses.png")
    }

    @Test
    fun settingsOpensLicenses() {
        var opened = false
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
                    onSelectSource = {},
                    onEditServer = {},
                    onWifiQualityChange = {},
                    onMobileQualityChange = {},
                    onSyncConcurrencyChange = {},
                    onDownloadQualityChange = {},
                    onDownloadOverMeteredChange = {},
                    onScrobblingChange = {},
                    onAddServer = {},
                    onOpenDownloads = {},
                    onOpenLicenses = { opened = true },
                    onBack = {},
                )
            }
        }
        awaitText("Add server")
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Licenses"))
        composeRule.onNodeWithText("Licenses").performClick()
        composeRule.waitForIdle()
        assertTrue(opened)
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private val fixture =
        """
        {
            "libraries": [
                {
                    "uniqueId": "com.squareup.okhttp3:okhttp",
                    "artifactVersion": "5.5.0",
                    "name": "OkHttp",
                    "description": "Square's HTTP client",
                    "website": "https://square.github.io/okhttp/",
                    "licenses": ["Apache-2.0"]
                },
                {
                    "uniqueId": "io.coil-kt.coil3:coil-compose",
                    "artifactVersion": "3.6.3",
                    "name": "Coil",
                    "description": "Image loading for Android",
                    "website": "https://coil-kt.github.io/coil/",
                    "licenses": ["Apache-2.0"]
                },
                {
                    "uniqueId": "com.google.guava:guava",
                    "artifactVersion": "33.0.0",
                    "name": "Guava",
                    "description": "Google core libraries",
                    "website": "https://github.com/google/guava",
                    "licenses": ["MIT"]
                }
            ],
            "licenses": {
                "Apache-2.0": {
                    "name": "Apache License 2.0",
                    "url": "https://spdx.org/licenses/Apache-2.0.html",
                    "spdxId": "Apache-2.0",
                    "hash": "Apache-2.0"
                },
                "MIT": {
                    "name": "MIT License",
                    "url": "https://spdx.org/licenses/MIT.html",
                    "spdxId": "MIT",
                    "hash": "MIT"
                }
            }
        }
        """.trimIndent()
}
