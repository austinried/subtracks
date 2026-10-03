package com.subtracks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.subtracks.ui.NowPlayingLauncher
import com.subtracks.ui.SubtracksRoot
import com.subtracks.ui.theme.SubtracksTheme
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    private val nowPlayingLauncher: NowPlayingLauncher by lazy { GlobalContext.get().get() }

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        if (shouldRecordNowPlayingLaunch(intent?.action, restoringState = savedInstanceState != null)) {
            nowPlayingLauncher.request()
        }
        setContent {
            SubtracksTheme {
                SubtracksRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_OPEN_NOW_PLAYING) nowPlayingLauncher.request()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        const val ACTION_OPEN_NOW_PLAYING = "com.subtracks.action.OPEN_NOW_PLAYING"
    }
}

internal fun shouldRecordNowPlayingLaunch(
    action: String?,
    restoringState: Boolean,
): Boolean = action == MainActivity.ACTION_OPEN_NOW_PLAYING && !restoringState
