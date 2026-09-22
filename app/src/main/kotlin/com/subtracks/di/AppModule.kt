package com.subtracks.di

import android.content.Context
import com.subtracks.data.db.createAndroidDatabase
import com.subtracks.data.prefs.createUserPreferences
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import com.subtracks.playback.MediaSessionConnection
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.PlayerConnection
import com.subtracks.ui.RootViewModel
import com.subtracks.ui.library.AlbumDetailViewModel
import com.subtracks.ui.library.LibraryViewModel
import com.subtracks.ui.library.PlaylistDetailViewModel
import com.subtracks.ui.settings.AddSourceViewModel
import com.subtracks.ui.settings.SettingsViewModel
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule(
    context: Context,
    http: OkHttpClient,
) = module {
    single { createAndroidDatabase(context) }
    single { http }
    single { createUserPreferences(context) }
    single { SourceRepository(get(), get(), get()) }
    single { LibraryRepository(get(), get()) }
    single { SyncManager(get()) }
    single { QueueRepository(get()) }
    single<PlayerConnection> { MediaSessionConnection(context.applicationContext, get()) }
    single { PlaybackController(get(), get(), get()) }
    viewModel { RootViewModel(get()) }
    viewModel { LibraryViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { AddSourceViewModel(get(), get()) }
    viewModel { params -> AlbumDetailViewModel(get(), get(), get(), params.get()) }
    viewModel { params -> PlaylistDetailViewModel(get(), get(), get(), params.get()) }
}
