package com.subtracks.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import coil3.SingletonImageLoader
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.subtracks.MainActivity
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.core.context.GlobalContext

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    private var artworkLoader: CoverArtBitmapLoader? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var sourceRepository: SourceRepository? = null
    private var library: LibraryRepository? = null
    private var currentSongId: String? = null
    private var starred = false
    private var starJob: Job? = null

    private val starCallback =
        object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
            ): MediaSession.ConnectionResult {
                val base = MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller).build()
                return MediaSession.ConnectionResult
                    .AcceptedResultBuilder(session, controller)
                    .setAvailableSessionCommands(
                        base.availableSessionCommands
                            .buildUpon()
                            .add(STAR_COMMAND)
                            .build(),
                    ).build()
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle,
            ): ListenableFuture<SessionResult> {
                if (customCommand.customAction != STAR_COMMAND.customAction) {
                    return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
                }
                toggleStar()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

    @OptIn(UnstableApi::class, ExperimentalApi::class)
    override fun onCreate() {
        super.onCreate()
        val sources = GlobalContext.get().get<SourceRepository>()
        sourceRepository = sources
        library = GlobalContext.get().get<LibraryRepository>()
        val exoPlayer =
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
                    DefaultMediaSourceFactory(this)
                        .setEnableClippingInMediaPeriod(true)
                        .setDataSourceFactory(mediaDataSourceFactory(this, OkHttpDataSource.Factory(streamingClient()))),
                ).build()
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    observeStar(mediaItem?.mediaId)
                }
            },
        )
        val loader =
            CoverArtBitmapLoader(
                applicationContext,
                sources,
                SingletonImageLoader.get(this),
            )
        artworkLoader = loader
        session =
            MediaSession
                .Builder(this, exoPlayer)
                .setSessionActivity(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, MainActivity::class.java)
                            .setAction(MainActivity.ACTION_OPEN_NOW_PLAYING),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                ).setBitmapLoader(loader)
                .setCallback(starCallback)
                .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    private fun observeStar(songId: String?) {
        starJob?.cancel()
        currentSongId = songId
        starred = false
        val library = library ?: return
        if (songId == null) {
            session?.setMediaButtonPreferences(emptyList())
            return
        }
        session?.setMediaButtonPreferences(listOf(starButton(false)))
        starJob =
            scope.launch {
                val sourceId = sourceRepository?.activeSourceIdOnce() ?: return@launch
                library.song(sourceId, songId).collect { song ->
                    if (currentSongId != songId) return@collect
                    starred = song?.starred != null
                    session?.setMediaButtonPreferences(listOf(starButton(starred)))
                }
            }
    }

    private fun toggleStar() {
        val songId = currentSongId ?: return
        val library = library ?: return
        library.toggleStar(StarType.Song, songId)
    }

    private fun starButton(isStarred: Boolean): CommandButton =
        CommandButton
            .Builder(if (isStarred) CommandButton.ICON_STAR_FILLED else CommandButton.ICON_STAR_UNFILLED)
            .setSessionCommand(STAR_COMMAND)
            .setDisplayName(if (isStarred) "Unstar" else "Star")
            .build()

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

    override fun onDestroy() {
        scope.cancel()
        artworkLoader?.shutdown()
        artworkLoader = null
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    private companion object {
        const val STREAM_TAG = "SubtracksPlayback"
        val STAR_COMMAND = SessionCommand("com.subtracks.STAR", Bundle.EMPTY)
    }
}

@OptIn(UnstableApi::class)
internal fun mediaDataSourceFactory(
    context: Context,
    upstream: DataSource.Factory,
): DataSource.Factory = DefaultDataSource.Factory(context, KnownLengthDataSourceFactory(upstream))
