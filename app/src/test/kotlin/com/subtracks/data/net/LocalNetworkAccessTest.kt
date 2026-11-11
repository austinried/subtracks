package com.subtracks.data.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

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

    @Test
    fun skipsTheCheckBeforeAndroid17() {
        assertFalse(check(sdk = 36))
    }

    @Test
    fun skipsTheCheckWhenThePermissionIsGranted() {
        assertFalse(check(granted = true))
    }

    @Test
    fun skipsAHostThatCannotBeParsed() {
        assertFalse(check(host = null))
    }

    @Test
    fun skipsWhenNoEligibleInterfaceExists() {
        assertFalse(check(hasEligibleInterface = false))
    }

    @Test
    fun requestsForADotLocalNameWithAnEligibleInterface() {
        assertTrue(check(host = "music.local", resolved = emptyList(), onLink = { false }))
    }

    @Test
    fun skipsADotLocalNameWithoutAnEligibleInterface() {
        assertFalse(check(host = "music.local", hasEligibleInterface = false))
    }

    @Test
    fun requestsForAnOnLinkAddress() {
        assertTrue(check(onLink = { it == address("10.0.0.5") }))
    }

    @Test
    fun skipsAnAddressThatIsNotOnLink() {
        assertFalse(check(onLink = { false }))
    }

    @Test
    fun skipsAnAddressRoutedThroughTheVpn() {
        assertFalse(check(onLink = { true }, vpnRouted = { true }))
    }

    @Test
    fun requestsWhenAnyCandidateIsOnLinkAndNotVpnRouted() {
        val onLink = address("10.0.0.5")
        assertTrue(
            check(
                resolved = listOf(address("10.0.0.6"), onLink),
                onLink = { it == onLink },
                vpnRouted = { it != onLink },
            ),
        )
    }

    @Test
    fun skipsWhenResolutionReturnedNothing() {
        assertFalse(check(resolved = emptyList()))
    }

    private fun check(
        sdk: Int = 37,
        granted: Boolean = false,
        host: String? = "10.0.0.5",
        hasEligibleInterface: Boolean = true,
        resolved: List<InetAddress> = listOf(address("10.0.0.5")),
        onLink: (InetAddress) -> Boolean = { it == address("10.0.0.5") },
        vpnRouted: (InetAddress) -> Boolean = { false },
    ): Boolean = needsPermission(sdk, granted, host, hasEligibleInterface, resolved, onLink, vpnRouted)

    private fun address(value: String): InetAddress = InetAddress.getByName(value)
}
