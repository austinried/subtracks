package com.subtracks.ui

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.awaitCancellation
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
            val dispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
            val overlayBackCallback =
                remember {
                    object : OnBackPressedCallback(false) {
                        override fun handleOnBackPressed() {
                            overlayOpen = false
                        }
                    }
                }
            SideEffect { overlayBackCallback.isEnabled = overlayOpen }
            Box(Modifier.fillMaxSize()) {
                NavHost(
                    navController = nav,
                    startDestination = "a",
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(260)) +
                            fadeIn(tween(260))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(260)) +
                            fadeOut(tween(260))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(260)) +
                            fadeIn(tween(260))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(260)) +
                            fadeOut(tween(260))
                    },
                ) {
                    composable("a") { Text("A") }
                    composable("b") { Text("B") }
                }
                if (overlayOpen) {
                    Text("OVERLAY", modifier = Modifier.fillMaxSize())
                }
            }
            // The NavHost registers its back callback from a LaunchedEffect, so the overlay must
            // register its own from a LaunchedEffect after it to land later in the dispatcher.
            LaunchedEffect(dispatcher, overlayBackCallback) {
                dispatcher.addCallback(overlayBackCallback)
                try {
                    awaitCancellation()
                } finally {
                    overlayBackCallback.remove()
                }
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
