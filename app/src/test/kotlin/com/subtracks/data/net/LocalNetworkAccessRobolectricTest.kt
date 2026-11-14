@file:Suppress("DEPRECATION")

package com.subtracks.data.net

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.IpPrefix
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.RouteInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo
import org.robolectric.util.ReflectionHelpers
import java.net.InetAddress

@RunWith(AndroidJUnit4::class)
@Config(sdk = [37])
class LocalNetworkAccessRobolectricTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    @Config(sdk = [36])
    fun skipsBeforeAndroid17() =
        runTest {
            activateWifi()
            assertFalse(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun requestsForAnAddressOnTheWifiSubnet() =
        runTest {
            activateWifi()
            assertTrue(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun skipsAnAddressOutsideTheWifiSubnet() =
        runTest {
            activateWifi()
            assertFalse(checker().requiresPermission("http://192.168.1.5"))
        }

    @Test
    fun skipsWhenThePermissionIsGranted() =
        runTest {
            activateWifi()
            shadowOf(application).grantPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)
            assertFalse(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun skipsWhenTheOnlyInterfaceIsCellular() =
        runTest {
            activate(ConnectivityManager.TYPE_MOBILE, NetworkCapabilities.TRANSPORT_CELLULAR)
            assertFalse(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun requestsForADotLocalNameWithWifi() =
        runTest {
            activateWifi()
            assertTrue(checker().requiresPermission("http://music.local"))
        }

    @Test
    fun skipsADotLocalNameWithoutAnEligibleInterface() =
        runTest {
            activate(ConnectivityManager.TYPE_MOBILE, NetworkCapabilities.TRANSPORT_CELLULAR)
            assertFalse(checker().requiresPermission("http://music.local"))
        }

    @Test
    fun skipsAnAddressRoutedThroughTheVpn() =
        runTest {
            addWifiNetwork()
            activateVpn(route("10.0.0.0", 24))
            assertFalse(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun requestsForALanAddressThatFallsThroughASplitTunnelVpn() =
        runTest {
            addWifiNetwork()
            activateVpn()
            assertTrue(checker().requiresPermission("http://10.0.0.5"))
        }

    @Test
    fun requestsForRoutesExcludedFromAFullTunnelVpn() =
        runTest {
            addWifiNetwork()
            activateVpn(
                route("0.0.0.0", 0),
                route("10.0.0.5", 32, RouteInfo.RTN_THROW),
            )
            assertTrue(checker().requiresPermission("http://10.0.0.5"))
            assertTrue(checker().requiresPermission("http://music.local"))
        }

    @Test
    fun skipsADotLocalNameThroughAFullTunnelVpn() =
        runTest {
            addWifiNetwork()
            activateVpn(route("0.0.0.0", 0))
            assertFalse(checker().requiresPermission("http://music.local"))
        }

    @Test
    fun requestsForDotLocalWhenOnlyIpv6UsesTheVpn() =
        runTest {
            addWifiNetwork()
            activateVpn(route("::", 0))
            assertTrue(checker().requiresPermission("http://music.local"))
        }

    private fun activateWifi() = activate(ConnectivityManager.TYPE_WIFI, NetworkCapabilities.TRANSPORT_WIFI)

    private fun activateVpn(vararg routes: RouteInfo) =
        activate(
            ConnectivityManager.TYPE_VPN,
            NetworkCapabilities.TRANSPORT_VPN,
            address = "100.64.0.2",
            prefix = 32,
            routes = routes.toList(),
        )

    private fun activate(
        type: Int,
        transport: Int,
        address: String = "10.0.0.2",
        prefix: Int = 24,
        routes: List<RouteInfo> = emptyList(),
    ) {
        shadowOf(manager()).setActiveNetworkInfo(info(type))
        configure(manager().activeNetwork!!, transport, address, prefix, routes)
    }

    private fun addWifiNetwork() {
        val network = ShadowNetwork.newInstance(7)
        shadowOf(manager()).addNetwork(network, info(ConnectivityManager.TYPE_WIFI))
        configure(network, NetworkCapabilities.TRANSPORT_WIFI, "10.0.0.2", 24)
    }

    private fun configure(
        network: Network,
        transport: Int,
        address: String,
        prefix: Int,
        routes: List<RouteInfo> = emptyList(),
    ) {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addTransportType(transport)
        shadowOf(manager()).setNetworkCapabilities(network, capabilities)
        shadowOf(manager()).setLinkProperties(
            network,
            LinkProperties().apply {
                setLinkAddresses(listOf(linkAddress(address, prefix)))
                routes.forEach { addRoute(it) }
            },
        )
    }

    private fun route(
        address: String,
        prefix: Int,
        type: Int = RouteInfo.RTN_UNICAST,
    ): RouteInfo =
        ReflectionHelpers.callConstructor(
            RouteInfo::class.java,
            ReflectionHelpers.ClassParameter.from(IpPrefix::class.java, IpPrefix(InetAddress.getByName(address), prefix)),
            ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType!!, type),
        )

    private fun linkAddress(
        address: String,
        prefix: Int,
    ): LinkAddress =
        ReflectionHelpers.callConstructor(
            LinkAddress::class.java,
            ReflectionHelpers.ClassParameter.from(InetAddress::class.java, InetAddress.getByName(address)),
            ReflectionHelpers.ClassParameter.from(Int::class.javaPrimitiveType!!, prefix),
        )

    private fun info(type: Int) =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            type,
            0,
            true,
            NetworkInfo.State.CONNECTED,
        )

    private fun checker() = LocalNetworkAccess(application)

    private fun manager(): ConnectivityManager = application.getSystemService(ConnectivityManager::class.java)
}
