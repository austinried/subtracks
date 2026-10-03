package com.subtracks.ui.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AddSourceViewModelTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var server: MockWebServer
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val createdViewModels = mutableListOf<AddSourceViewModel>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        val artwork = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
        sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), artwork, scope = repoScope)
        downloadRepository =
            DownloadRepository(
                db,
                sourceRepository,
                FakeDownloadEngine(),
                File(context.cacheDir, "downloads-${System.nanoTime()}"),
                artworkStore = artwork,
                artworkFetcher = { ByteArray(0) },
                scope = repoScope,
            )
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        createdViewModels.forEach { runBlocking { it.viewModelScope.coroutineContext[Job]?.cancelAndJoin() } }
        cancelAndJoinBlocking(repoScope)
        server.shutdown()
        db.close()
        Dispatchers.resetMain()
        dispatcher.close()
    }

    @Test
    fun aRejectedPingSetsTheErrorAndClearsBusy() {
        val viewModel = viewModel()
        viewModel.setAddress(server.url("/").toString())
        viewModel.setUsername("u")
        viewModel.setPassword("p")
        server.enqueue(failed())

        viewModel.testConnection()
        await { !viewModel.state.value.busy }

        val state = viewModel.state.value
        assertFalse(state.busy)
        assertTrue("a rejected ping should be an error", state.isError)
        assertEquals("Failed: Wrong username", state.message)
    }

    @Test
    fun aMalformedResponseIsReportedWithTheLocalizedMessage() {
        val viewModel = viewModel()
        viewModel.setAddress(server.url("/").toString())
        viewModel.setUsername("u")
        viewModel.setPassword("p")
        server.enqueue(MockResponse().setBody("not an xml document"))

        viewModel.testConnection()
        await { !viewModel.state.value.busy && viewModel.state.value.isError }

        assertEquals("Malformed response from the server", viewModel.state.value.message)
    }

    @Test
    fun aSuccessfulPingClearsAnEarlierError() {
        val viewModel = viewModel()
        viewModel.setAddress(server.url("/").toString())
        viewModel.setUsername("u")
        viewModel.setPassword("p")
        server.enqueue(failed())
        viewModel.testConnection()
        await { !viewModel.state.value.busy && viewModel.state.value.isError }

        server.enqueue(ok())
        viewModel.testConnection()
        await { !viewModel.state.value.busy && !viewModel.state.value.isError }

        val state = viewModel.state.value
        assertEquals("Connection OK", state.message)
        assertFalse(state.isError)
    }

    @Test
    fun theActiveSourceCannotBeDeleted() =
        runBlocking {
            val sourceId = sourceRepository.addSource("nav", server.url("/").toString(), "u", "p", false)
            val viewModel = viewModel(sourceId)
            await { !viewModel.state.value.canDelete }

            viewModel.delete {}
            await { !viewModel.state.value.busy && viewModel.state.value.message != null }

            val state = viewModel.state.value
            assertTrue(state.isError)
            assertTrue(state.message!!.contains("active server can't be deleted"))
            assertNotNull("the active source must survive the refused delete", db.sourcesDao().sourceOnce(sourceId))
        }

    private fun viewModel(sourceId: Long? = null): AddSourceViewModel {
        val syncManager = SyncManager(db, sourceRepository, QueueRepository(db), scope = repoScope)
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        return AddSourceViewModel(sourceRepository, syncManager, downloadRepository, sourceId, resources).also { createdViewModels += it }
    }

    private fun ok() = MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>")

    private fun failed() =
        MockResponse().setBody(
            "<subsonic-response status=\"failed\"><error code=\"40\" message=\"Wrong username\"/></subsonic-response>",
        )

    private fun await(predicate: () -> Boolean) = awaitUntil("waiting for the expected state", predicate)
}
