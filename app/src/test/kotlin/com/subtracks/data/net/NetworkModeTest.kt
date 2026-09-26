package com.subtracks.data.net

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkModeTest {
    @Test
    fun meteredIsMobileAndUnmeteredIsWifi() {
        assertEquals(NetworkMode.Mobile, networkModeFor(true, NetworkMode.Wifi))
        assertEquals(NetworkMode.Wifi, networkModeFor(false, NetworkMode.Mobile))
    }

    @Test
    fun noActiveNetworkKeepsTheLastMode() {
        assertEquals(NetworkMode.Wifi, networkModeFor(null, NetworkMode.Wifi))
        assertEquals(NetworkMode.Mobile, networkModeFor(null, NetworkMode.Mobile))
    }
}
