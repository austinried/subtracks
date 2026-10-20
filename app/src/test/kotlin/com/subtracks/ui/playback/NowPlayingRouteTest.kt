package com.subtracks.ui.playback

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.TEST_TIMEOUT_MS
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import com.subtracks.playback.RepeatMode
import com.subtracks.ui.theme.SubtracksTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NowPlayingRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val controllerScope = CoroutineScope(SupervisorJob() + dispatcher)

    private lateinit var db: SubtracksDatabase
    private lateinit var handle: FakePlayerHandle
    private lateinit var controller: PlaybackController
    private lateinit var library: LibraryRepository
    private lateinit var downloads: DownloadRepository

    private val prefs = fakeUserPreferences()

    private val serverActions =
        object : ServerActionSink {
            override suspend fun nowPlaying(songId: String) = Unit

            override suspend fun scrobble(
                songId: String,
                time: Long,
            ) = Unit

            override suspend fun setStar(
                type: StarType,
                id: String,
                starred: Boolean,
            ) = Unit
        }

    private fun setUpState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val artworkStore = ArtworkStore(File(context.cacheDir, "art"))
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        val sources = SourceRepository(db, OkHttpClient(), prefs, artworkStore, scope = repoScope)
        val queues = QueueRepository(db)
        downloads =
            DownloadRepository(
                db,
                sources,
                FakeDownloadEngine(),
                File(context.cacheDir, "downloads-${System.nanoTime()}"),
                artworkStore = artworkStore,
                artworkFetcher = { ByteArray(0) },
                scope = repoScope,
            ).also { it.start() }
        library = LibraryRepository(db, sources, serverActions, prefs, scope = repoScope)
        handle = FakePlayerHandle()
        controller =
            PlaybackController(
                sources,
                queues,
                FakePlayerConnection(handle),
                downloads,
                showMessage = {},
                scope = controllerScope,
            )
    }

    private fun seedAlbum(count: Int) {
        runBlocking {
            db.sourcesDao().upsertSource(Source(1, "source 1", "http://localhost:1", true, 1))
            db.libraryDao().upsertSongs(
                (1..count).map { track ->
                    Song(
                        sourceId = 1,
                        id = "s$track",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song $track",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = track.toLong(),
                        disc = 1,
                        starred = null,
                        genre = null,
                    )
                },
            )
        }
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(controllerScope, repoScope)
        db.close()
        dispatcher.close()
    }

    @Test
    fun togglingRepeatRefreshesTheAdjacentArtwork() {
        setUpState()
        seedAlbum(3)
        controller.playAlbum(1, "al1", 0)
        awaitUntil("waiting for the first track") {
            controller.state.value.item
                ?.id == "s1"
        }

        composeRule.setContent {
            SubtracksTheme {
                NowPlayingRoute(
                    onBack = {},
                    onQueue = {},
                    onViewAlbum = {},
                    onViewArtist = {},
                    setStar = { _, _, _ -> },
                    controller = controller,
                    libraryRepository = library,
                    downloadRepository = downloads,
                    userPreferences = prefs,
                )
            }
        }
        composeRule.waitUntil(TEST_TIMEOUT_MS) {
            composeRule
                .onAllNodesWithTag(NOW_PLAYING_NEXT_TAG, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithTag(NOW_PLAYING_PREVIOUS_TAG, useUnmergedTree = true)
            .assertDoesNotExist()

        controller.cycleRepeat()
        assertEquals(RepeatMode.All, controller.state.value.repeat)

        composeRule.waitUntil(TEST_TIMEOUT_MS) {
            composeRule
                .onAllNodesWithTag(NOW_PLAYING_PREVIOUS_TAG, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
