package com.subtracks.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class LocalNetworkAccessTest {
    @Test
    fun matchesAnAddressInsideASlash24Network() {
        assertTrue(address("192.168.1.122").isWithinPrefix(address("192.168.1.243"), 24))
    }

    @Test
    fun rejectsAnAddressOutsideASlash24Network() {
        assertFalse(address("10.224.202.10").isWithinPrefix(address("192.168.22.11"), 24))
    }

    @Test
    fun matchesAnAddressInsideASlash8Network() {
        assertTrue(address("10.224.202.10").isWithinPrefix(address("10.0.0.1"), 8))
    }

    @Test
    fun masksThePartialByteOfANonOctetPrefix() {
        assertTrue(address("192.168.1.200").isWithinPrefix(address("192.168.1.130"), 25))
        assertFalse(address("192.168.1.100").isWithinPrefix(address("192.168.1.130"), 25))
    }

    @Test
    fun doesNotMatchAcrossAddressFamilies() {
        assertFalse(address("::1").isWithinPrefix(address("10.0.0.1"), 8))
        assertFalse(address("10.0.0.1").isWithinPrefix(address("fd00::1"), 64))
    }

    @Test
    fun matchesAnIpv6Prefix() {
        assertTrue(address("fd00:1234:5678:9abc::5").isWithinPrefix(address("fd00:1234:5678:9abc::1"), 64))
        assertFalse(address("fd00:1234:5678:9abd::5").isWithinPrefix(address("fd00:1234:5678:9abc::1"), 64))
    }

    @Test
    fun matchesAHostRoute() {
        assertTrue(address("10.0.0.5").isWithinPrefix(address("10.0.0.5"), 32))
        assertFalse(address("10.0.0.6").isWithinPrefix(address("10.0.0.5"), 32))
    }

    @Test
    fun extractsTheHostFromAServerAddress() {
        assertEquals("navidrome.example.com", serverHost("https://navidrome.example.com/music/"))
        assertEquals("navidrome.example.com", serverHost("navidrome.example.com:4533"))
        assertEquals("10.224.202.10", serverHost("http://10.224.202.10"))
    }

    private fun address(value: String): InetAddress = InetAddress.getByName(value)
}
