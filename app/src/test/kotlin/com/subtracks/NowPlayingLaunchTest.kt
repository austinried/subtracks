package com.subtracks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingLaunchTest {
    @Test
    fun aFreshLaunchFromTheNotificationIsRecorded() {
        assertTrue(
            shouldRecordNowPlayingLaunch(
                action = MainActivity.ACTION_OPEN_NOW_PLAYING,
                restoringState = false,
            ),
        )
    }

    @Test
    fun recreatingTheActivityDoesNotReRecordTheLaunch() {
        assertFalse(
            shouldRecordNowPlayingLaunch(
                action = MainActivity.ACTION_OPEN_NOW_PLAYING,
                restoringState = true,
            ),
        )
    }

    @Test
    fun aNormalLaunchIsNotRecorded() {
        assertFalse(shouldRecordNowPlayingLaunch(action = "android.intent.action.MAIN", restoringState = false))
    }

    @Test
    fun aLaunchWithoutAnActionIsNotRecorded() {
        assertFalse(shouldRecordNowPlayingLaunch(action = null, restoringState = false))
    }
}
