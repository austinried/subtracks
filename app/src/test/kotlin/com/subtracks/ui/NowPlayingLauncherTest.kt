package com.subtracks.ui

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NowPlayingLauncherTest {
    @Test
    fun aRequestMadeBeforeCollectionIsDelivered() =
        runTest {
            val launcher = NowPlayingLauncher()
            launcher.request()
            assertEquals(Unit, launcher.openNowPlaying.first())
        }

    @Test
    fun repeatedRequestsCollapseToTheLatest() =
        runTest {
            val launcher = NowPlayingLauncher()
            repeat(3) { launcher.request() }
            assertEquals(Unit, launcher.openNowPlaying.first())
            assertNull(withTimeoutOrNull(100) { launcher.openNowPlaying.first() })
        }
}
