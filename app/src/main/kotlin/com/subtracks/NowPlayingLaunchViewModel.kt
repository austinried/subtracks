package com.subtracks

import androidx.lifecycle.ViewModel

class NowPlayingLaunchViewModel : ViewModel() {
    private var launchHandled = false

    fun claimLaunch(action: String?): Boolean {
        if (action != MainActivity.ACTION_OPEN_NOW_PLAYING || launchHandled) return false
        launchHandled = true
        return true
    }

    fun markHandled() {
        launchHandled = true
    }
}
