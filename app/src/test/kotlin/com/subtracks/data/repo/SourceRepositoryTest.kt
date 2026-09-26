package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourceRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var repository: SourceRepository
    private val messages = ArrayList<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        repository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), showMessage = { messages += it })
    }

    @After
    fun tearDown() {
        repository.close()
        db.close()
    }

    @Test
    fun addingASourceMakesItTheSingleActiveOne() =
        runTest {
            repository.addSource("first", "http://a.example", "u", "p", true)
            val firstId = repository.activeSourceId().first()

            repository.addSource("second", "http://b.example", "u", "p", true)

            assertEquals(1, repository.sources().first().count { it.isActive })
            assertEquals("second", repository.activeConfig().first()?.name)
            assertEquals(
                "http://a.example/",
                repository
                    .sources()
                    .first()
                    .first { it.id == firstId }
                    .address,
            )
        }

    @Test
    fun aTokenAuthSourceIsProbedAndSwitchedToPassword() =
        runBlocking {
            withServer { server ->
                server.enqueue(MockResponse().setBody(failed(41)))
                server.enqueue(MockResponse().setBody(OK))

                repository.addSource("nav", server.url("/").toString(), "u", "s3cret", true)

                val config = withTimeout(5_000) { repository.activeConfig().first { it != null && !it.useTokenAuth } }
                assertFalse(config!!.useTokenAuth)
                withTimeout(5_000) { while (messages.isEmpty()) delay(10) }
                assertEquals(1, messages.size)
            }
        }

    @Test
    fun pingReportsWhetherItFellBackToThePassword() =
        runBlocking {
            withServer { server ->
                server.enqueue(MockResponse().setBody(failed(41)))
                server.enqueue(MockResponse().setBody(OK))
                server.enqueue(MockResponse().setBody(OK))

                assertTrue(repository.ping(server.url("/").toString(), "u", "s3cret", true).getOrThrow())
                assertFalse(repository.ping(server.url("/").toString(), "u", "s3cret", false).getOrThrow())
            }
        }

    private suspend fun withServer(block: suspend (MockWebServer) -> Unit) {
        val server = MockWebServer()
        server.start()
        try {
            block(server)
        } finally {
            server.shutdown()
        }
    }

    private fun failed(code: Int) = "<subsonic-response status=\"failed\"><error code=\"$code\" message=\"nope\"/></subsonic-response>"

    private companion object {
        const val OK = "<subsonic-response status=\"ok\" version=\"1.16.1\"/>"
    }
}
