package com.subtracks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingLaunchViewModelTest {
    @Test
    fun aFreshLaunchFromTheNotificationIsClaimed() {
        assertTrue(NowPlayingLaunchViewModel().claimLaunch(MainActivity.ACTION_OPEN_NOW_PLAYING))
    }

    @Test
    fun recreatingTheActivityDoesNotReClaimTheLaunch() {
        val viewModel = NowPlayingLaunchViewModel()
        assertTrue(viewModel.claimLaunch(MainActivity.ACTION_OPEN_NOW_PLAYING))
        assertFalse(viewModel.claimLaunch(MainActivity.ACTION_OPEN_NOW_PLAYING))
    }

    @Test
    fun aWarmTapMarksTheLaunchHandled() {
        val viewModel = NowPlayingLaunchViewModel()
        viewModel.markHandled()
        assertFalse(viewModel.claimLaunch(MainActivity.ACTION_OPEN_NOW_PLAYING))
    }

    @Test
    fun aNormalLaunchIsNotClaimed() {
        assertFalse(NowPlayingLaunchViewModel().claimLaunch("android.intent.action.MAIN"))
    }

    @Test
    fun aLaunchWithoutAnActionIsNotClaimed() {
        assertFalse(NowPlayingLaunchViewModel().claimLaunch(null))
    }
}
