package com.subtracks.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil3.SingletonImageLoader
import com.subtracks.MainActivity
import com.subtracks.data.repo.SourceRepository
import okhttp3.OkHttpClient
import org.koin.core.context.GlobalContext

class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player =
            ExoPlayer
                .Builder(this)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true,
                ).setHandleAudioBecomingNoisy(true)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(this).setDataSourceFactory(OkHttpDataSource.Factory(streamingClient())),
                ).build()
        session =
            MediaSession
                .Builder(this, player)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                ).setBitmapLoader(
                    CoverArtBitmapLoader(
                        this,
                        GlobalContext.get().get<SourceRepository>(),
                        SingletonImageLoader.get(this),
                    ),
                ).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    private fun streamingClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                val started = SystemClock.elapsedRealtime()
                val response = chain.proceed(chain.request())
                Log.d(
                    STREAM_TAG,
                    "stream range=${chain.request().header("Range")} code=${response.code} " +
                        "contentRange=${response.header("Content-Range")} " +
                        "length=${response.body.contentLength()} in ${SystemClock.elapsedRealtime() - started}ms",
                )
                response
            }.build()

    private companion object {
        const val STREAM_TAG = "SubtracksPlayback"
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
