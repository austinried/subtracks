package com.subtracks.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalNetworkAccessTest {
    @Test
    fun treatsDotLocalHostnamesAsLocalNetwork() {
        assertTrue(isLocalHostname("navidrome.local"))
        assertTrue(isLocalHostname("NAVIDROME.LOCAL."))
        assertTrue(isLocalHostname("local"))
        assertFalse(isLocalHostname("navidrome.example.com"))
        assertFalse(isLocalHostname("local.example.com"))
    }

    @Test
    fun extractsTheHostFromAServerAddress() {
        assertEquals("navidrome.example.com", serverHost("https://navidrome.example.com/music/"))
        assertEquals("navidrome.example.com", serverHost("navidrome.example.com:4533"))
        assertEquals("10.224.202.10", serverHost("http://10.224.202.10"))
    }
}
