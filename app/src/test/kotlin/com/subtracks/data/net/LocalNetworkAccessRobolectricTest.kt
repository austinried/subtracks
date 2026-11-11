@file:Suppress("DEPRECATION")

package com.subtracks.data.net

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
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
    fun requestsForALanAddressThatFallsThroughASplitTunnelVpn() =
        runTest {
            addWifiNetwork()
            activate(ConnectivityManager.TYPE_VPN, NetworkCapabilities.TRANSPORT_VPN, address = "100.64.0.2", prefix = 32)
            assertTrue(checker().requiresPermission("http://10.0.0.5"))
        }

    private fun activateWifi() = activate(ConnectivityManager.TYPE_WIFI, NetworkCapabilities.TRANSPORT_WIFI)

    private fun activate(
        type: Int,
        transport: Int,
        address: String = "10.0.0.2",
        prefix: Int = 24,
    ) {
        shadowOf(manager()).setActiveNetworkInfo(info(type))
        configure(manager().activeNetwork!!, transport, address, prefix)
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
    ) {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addTransportType(transport)
        shadowOf(manager()).setNetworkCapabilities(network, capabilities)
        shadowOf(manager()).setLinkProperties(
            network,
            LinkProperties().apply {
                setLinkAddresses(listOf(linkAddress(address, prefix)))
            },
        )
    }

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
