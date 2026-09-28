package com.subtracks.di

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.subtracks.data.db.createAndroidDatabase
import com.subtracks.data.download.ArtworkFetcher
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.DownloadEngine
import com.subtracks.data.download.OkHttpArtworkFetcher
import com.subtracks.data.download.SystemDownloadEngine
import com.subtracks.data.net.networkMode
import com.subtracks.data.prefs.createUserPreferences
import com.subtracks.data.repo.ArtworkSeedRepository
import com.subtracks.data.repo.ArtworkSeedStore
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import com.subtracks.playback.MediaSessionConnection
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.PlayerConnection
import com.subtracks.ui.RootViewModel
import com.subtracks.ui.downloads.DownloadsViewModel
import com.subtracks.ui.library.AlbumDetailViewModel
import com.subtracks.ui.library.ArtistDetailViewModel
import com.subtracks.ui.library.LibraryViewModel
import com.subtracks.ui.library.PlaylistDetailViewModel
import com.subtracks.ui.playback.QueueViewModel
import com.subtracks.ui.settings.AddSourceViewModel
import com.subtracks.ui.settings.SettingsViewModel
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.io.File

fun appModule(
    context: Context,
    http: OkHttpClient,
) = module {
    val mainHandler = Handler(Looper.getMainLooper())
    val toast: (String) -> Unit = { message ->
        mainHandler.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }
    single { createAndroidDatabase(context) }
    single { http }
    single { createUserPreferences(context) }
    single { SourceRepository(get(), get(), get(), get(), networkMode = networkMode(context.applicationContext), showMessage = toast) }
    single { LibraryRepository(get(), get(), toast) }
    single<ArtworkSeedStore> { ArtworkSeedRepository(get()) }
    single { SyncManager(get(), get(), get()) }
    single { QueueRepository(get()) }
    single { ArtworkStore(downloadsRoot(context)) }
    single<ArtworkFetcher> { OkHttpArtworkFetcher(get()) }
    single<DownloadEngine> { SystemDownloadEngine(context.applicationContext) }
    single {
        DownloadRepository(
            db = get(),
            sourceRepository = get(),
            engine = get(),
            downloadsDir = downloadsRoot(context),
            artworkStore = get(),
            artworkFetcher = get(),
            showMessage = toast,
        ).also { it.start() }
    }
    single<PlayerConnection> { MediaSessionConnection(context.applicationContext, get(), get()) }
    single { PlaybackController(get(), get(), get(), get(), showMessage = toast) }
    viewModel { RootViewModel(get()) }
    viewModel { LibraryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { DownloadsViewModel(get(), get(), get()) }
    viewModel { AddSourceViewModel(get(), get()) }
    viewModel { params -> AlbumDetailViewModel(get(), get(), get(), get(), params.get()) }
    viewModel { params -> ArtistDetailViewModel(get(), get(), get(), get(), params.get()) }
    viewModel { params -> PlaylistDetailViewModel(get(), get(), get(), get(), params.get()) }
    viewModel { QueueViewModel(get(), get()) }
}

private fun downloadsRoot(context: Context): File =
    File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir, "downloads")
