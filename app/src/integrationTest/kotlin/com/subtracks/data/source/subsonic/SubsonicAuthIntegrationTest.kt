package com.subtracks.data.source.subsonic

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SubsonicAuthIntegrationTest(
    private val server: TestServer,
) {
    @Test
    fun tokenAuthFallsBackToPlaintextWhenTheServerRejectsIt() =
        runBlocking {
            var fallbacks = 0
            val source = SubsonicSource(1, TestServers.client(server, useTokenAuth = true) { fallbacks++ })

            source.ping()

            assertEquals("$server: fallback fired once", !server.supportsTokenAuth, fallbacks == 1)

            source.ping()

            assertEquals("$server: fallback is not retried", if (server.supportsTokenAuth) 0 else 1, fallbacks)
        }

    @Test
    fun plaintextAuthPings() =
        runBlocking {
            TestServers.source(server, useTokenAuth = false).ping()
        }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> = TestServers.all.map { arrayOf<Any>(it) }
    }
}
