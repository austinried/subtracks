package com.subtracks.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.subtracks.data.repo.SourceRepository
import java.util.concurrent.Executor

interface PlayerConnection {
    fun connect(
        onConnected: (PlayerHandle) -> Unit,
        onDisconnected: () -> Unit,
    )
}

class MediaSessionConnection(
    private val context: Context,
    private val sourceRepository: SourceRepository,
) : PlayerConnection {
    override fun connect(
        onConnected: (PlayerHandle) -> Unit,
        onDisconnected: () -> Unit,
    ) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future =
            MediaController
                .Builder(context, token)
                .setListener(
                    object : MediaController.Listener {
                        override fun onDisconnected(controller: MediaController) = onDisconnected()
                    },
                ).buildAsync()
        future.addListener(
            {
                val controller = runCatching { future.get() }.getOrNull()
                if (controller == null) onDisconnected() else onConnected(Media3PlayerHandle(controller, sourceRepository))
            },
            Executor { it.run() },
        )
    }
}
