package com.subtracks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.subtracks.ui.SubtracksRoot
import com.subtracks.ui.theme.SubtracksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubtracksTheme {
                SubtracksRoot()
            }
        }
    }
}
