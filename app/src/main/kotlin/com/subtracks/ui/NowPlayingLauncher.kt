package com.subtracks.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class NowPlayingLauncher {
    private val requests = Channel<Unit>(Channel.CONFLATED)

    val openNowPlaying: Flow<Unit> = requests.receiveAsFlow()

    fun request() {
        requests.trySend(Unit)
    }
}
