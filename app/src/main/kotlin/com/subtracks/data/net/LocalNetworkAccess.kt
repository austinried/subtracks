package com.subtracks.data.net

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.IpPrefix
import android.net.LinkAddress
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

internal const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

private const val LOCAL_NETWORK_PERMISSION_SDK = 37

class LocalNetworkAccess(
    private val context: Context,
) {
    private val deniedAddresses = mutableSetOf<String>()

    suspend fun requiresPermission(address: String): Boolean {
        val granted = granted()
        if (Build.VERSION.SDK_INT < LOCAL_NETWORK_PERMISSION_SDK || granted) return false
        val host = serverHost(address) ?: return false
        return withContext(Dispatchers.IO) {
            val resolved = if (isLocalHostname(host)) emptyList() else resolve(host)
            val linkAddresses = currentLinkAddresses()
            needsPermission(
                sdk = Build.VERSION.SDK_INT,
                granted = granted,
                host = host,
                hasEligibleInterface = linkAddresses.isNotEmpty(),
                resolved = resolved,
                onLink = { candidate -> linkAddresses.any { it.contains(candidate) } },
                vpnRouted = ::routedThroughVpn,
            )
        }
    }

    @Synchronized
    fun markDenied(address: String) {
        deniedAddresses += normalizeServerAddress(address)
    }

    @Synchronized
    fun wasDenied(address: String): Boolean = normalizeServerAddress(address) in deniedAddresses

    private fun granted(): Boolean =
        ContextCompat.checkSelfPermission(context, LOCAL_NETWORK_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun resolve(host: String): List<InetAddress> = runCatching { InetAddress.getAllByName(host).toList() }.getOrDefault(emptyList())

    @Suppress("DEPRECATION")
    private fun currentLinkAddresses(): List<LinkAddress> {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return emptyList()
        return manager.allNetworks
            .filter { network -> manager.getNetworkCapabilities(network)?.isLocalNetworkInterface == true }
            .flatMap { network -> manager.getLinkProperties(network)?.linkAddresses.orEmpty() }
    }

    @RequiresApi(LOCAL_NETWORK_PERMISSION_SDK)
    private fun routedThroughVpn(address: InetAddress): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return false
        val properties = manager.getLinkProperties(network) ?: return false
        return properties.routes.any { route -> route.destination.contains(address) }
    }
}

internal fun needsPermission(
    sdk: Int,
    granted: Boolean,
    host: String?,
    hasEligibleInterface: Boolean,
    resolved: List<InetAddress>,
    onLink: (InetAddress) -> Boolean,
    vpnRouted: (InetAddress) -> Boolean,
): Boolean {
    if (sdk < LOCAL_NETWORK_PERMISSION_SDK || granted || host == null || !hasEligibleInterface) return false
    if (isLocalHostname(host)) return true
    return resolved.any { candidate -> !vpnRouted(candidate) && onLink(candidate) }
}

internal fun isLocalHostname(host: String): Boolean {
    val normalized = host.lowercase().trimEnd('.')
    return normalized == "local" || normalized.endsWith(".local")
}

@RequiresApi(LOCAL_NETWORK_PERMISSION_SDK)
private fun LinkAddress.contains(address: InetAddress): Boolean = IpPrefix(this.address, prefixLength).contains(address)

private val NetworkCapabilities.isLocalNetworkInterface: Boolean
    get() =
        !hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
            !hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
