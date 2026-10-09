package com.subtracks

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserAgentTest {
    @Test
    fun theSharedClientSendsTheSubtracksUserAgent() {
        val server = MockWebServer()
        server.enqueue(MockResponse())
        server.start()

        UserAgent
            .httpClient()
            .newCall(Request.Builder().url(server.url("/")).build())
            .execute()
            .close()
        val header = server.takeRequest().getHeader("User-Agent")
        server.shutdown()

        assertEquals("subtracks (Android ${Build.VERSION.RELEASE}; ${BuildConfig.VERSION_NAME})", header)
    }
}
