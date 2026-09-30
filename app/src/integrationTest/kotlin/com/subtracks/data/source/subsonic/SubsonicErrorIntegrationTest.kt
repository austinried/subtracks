package com.subtracks.data.source.subsonic

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SubsonicErrorIntegrationTest(
    private val server: TestServer,
) {
    @Test
    fun wrongPasswordThrowsCode40() =
        runBlocking {
            val bad = server.copy(password = "${server.password}-wrong")
            val source = SubsonicSource(1, TestServers.client(bad))

            val failure = runCatching { source.ping() }

            val error = failure.exceptionOrNull()
            assertTrue("$server: $failure", error is SubsonicException)
            assertEquals("$server", 40, (error as SubsonicException).code)
        }

    @Test
    fun unknownMethodThrows() =
        runBlocking {
            val client = TestServers.client(server)

            val failure = runCatching { client.check("definitelyNotASubsonicMethod") }

            assertTrue("$server: $failure", failure.exceptionOrNull() is SubsonicException)
        }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> = TestServers.all.map { arrayOf<Any>(it) }
    }
}
