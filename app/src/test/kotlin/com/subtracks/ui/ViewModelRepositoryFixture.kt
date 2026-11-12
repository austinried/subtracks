package com.subtracks.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.createTestDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.NetworkServerActionSink
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelRepositoryFixture {
    val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    init {
        Dispatchers.setMain(dispatcher)
    }

    val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val controllerScope = CoroutineScope(SupervisorJob() + dispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = createTestDatabase()
    val artworkStore = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
    val downloadEngine = FakeDownloadEngine()
    val downloadsDir = File(context.cacheDir, "downloads-${System.nanoTime()}")
    val sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), artworkStore, scope = repoScope)
    val queueRepository = QueueRepository(db)
    val downloadRepository =
        DownloadRepository(
            db,
            sourceRepository,
            downloadEngine,
            downloadsDir,
            artworkStore = artworkStore,
            artworkFetcher = { ByteArray(0) },
            queueRepository = queueRepository,
            scope = repoScope,
        )
    val controller =
        PlaybackController(
            sourceRepository,
            queueRepository,
            FakePlayerConnection(FakePlayerHandle()),
            downloadRepository,
            fakeUserPreferences(),
            scope = controllerScope,
        )
    val libraryRepository =
        LibraryRepository(
            db,
            sourceRepository,
            NetworkServerActionSink(sourceRepository),
            fakeUserPreferences(),
            queueRepository = queueRepository,
            scope = repoScope,
        )

    fun close() {
        cancelAndJoinBlocking(controllerScope, repoScope)
        db.close()
        Dispatchers.resetMain()
        dispatcher.close()
    }
}
