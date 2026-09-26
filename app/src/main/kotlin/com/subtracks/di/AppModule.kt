package com.subtracks.di

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.subtracks.data.db.createAndroidDatabase
import com.subtracks.data.net.networkMode
import com.subtracks.data.prefs.createUserPreferences
import com.subtracks.data.repo.ArtworkSeedRepository
import com.subtracks.data.repo.ArtworkSeedStore
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import com.subtracks.playback.MediaSessionConnection
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.PlayerConnection
import com.subtracks.ui.RootViewModel
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
    single { SourceRepository(get(), get(), get(), networkMode = networkMode(context.applicationContext), showMessage = toast) }
    single { LibraryRepository(get(), get(), toast) }
    single<ArtworkSeedStore> { ArtworkSeedRepository(get()) }
    single { SyncManager(get(), get(), get()) }
    single { QueueRepository(get()) }
    single<PlayerConnection> { MediaSessionConnection(context.applicationContext, get()) }
    single { PlaybackController(get(), get(), get()) }
    viewModel { RootViewModel(get()) }
    viewModel { LibraryViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { AddSourceViewModel(get(), get()) }
    viewModel { params -> AlbumDetailViewModel(get(), get(), get(), params.get()) }
    viewModel { params -> ArtistDetailViewModel(get(), get(), get(), params.get()) }
    viewModel { params -> PlaylistDetailViewModel(get(), get(), get(), params.get()) }
    viewModel { QueueViewModel(get(), get()) }
}
