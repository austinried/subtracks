package com.subtracks.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun defaultSchemeIsNotMonochrome() {
        var scheme: ColorScheme? = null
        composeRule.setContent {
            SubtracksTheme {
                scheme = MaterialTheme.colorScheme
            }
        }
        val resolved = requireNotNull(scheme)
        assertNotEquals(Color.White, resolved.primary)
        assertNotEquals(Color.Black, resolved.background)
    }
}
