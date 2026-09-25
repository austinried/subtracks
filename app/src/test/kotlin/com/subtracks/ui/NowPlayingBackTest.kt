package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class NowPlayingBackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backClosesTheOverlayBeforePoppingTheDestination() {
        var overlayOpen by mutableStateOf(false)
        var navController: NavHostController? = null
        composeRule.setContent {
            val nav = rememberNavController()
            navController = nav
            Box(Modifier.fillMaxSize()) {
                NavHost(navController = nav, startDestination = "a") {
                    composable("a") { Text("A") }
                    composable("b") { Text("B") }
                }
                if (overlayOpen) {
                    Text("OVERLAY", modifier = Modifier.fillMaxSize())
                }
            }
            // Same ordering as MainNavigation: the overlay's back handler is registered after the
            // NavHost, so it must consume back and leave the destination in place.
            BackHandler(enabled = overlayOpen) {
                overlayOpen = false
            }
        }

        composeRule.runOnIdle { navController!!.navigate("b") }
        composeRule.onNodeWithText("B").assertExists()
        composeRule.runOnIdle { overlayOpen = true }
        composeRule.onNodeWithText("OVERLAY").assertExists()

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertFalse("the overlay should have closed", overlayOpen)
        composeRule.onNodeWithText("B").assertExists()
    }
}
