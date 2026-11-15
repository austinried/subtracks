package com.subtracks.data.net

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.IpPrefix
import android.net.LinkAddress
import android.net.NetworkCapabilities
import android.net.RouteInfo
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

internal const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

class LocalNetworkAccess(
    private val context: Context,
) {
    @Suppress("DEPRECATION")
    suspend fun requiresPermission(address: String): Boolean {
        if (Build.VERSION.SDK_INT < 37 || granted()) return false
        val host = serverHost(address) ?: return false
        return withContext(Dispatchers.IO) {
            val resolved = if (isLocalHostname(host)) emptyList() else resolve(host)
            val manager = context.getSystemService(ConnectivityManager::class.java) ?: return@withContext false
            val linkAddresses =
                manager.allNetworks
                    .filter { network -> manager.getNetworkCapabilities(network)?.isPhysicalLan == true }
                    .flatMap { network -> manager.getLinkProperties(network)?.linkAddresses.orEmpty() }
            if (linkAddresses.isEmpty()) return@withContext false

            val vpnRoutes = activeVpnRoutes(manager)
            if (isLocalHostname(host)) {
                vpnRoutes.mayFallThroughToLan(linkAddresses)
            } else {
                resolved.any { candidate ->
                    linkAddresses.any { it.contains(candidate) } && vpnRoutes.fallsThroughToLan(candidate)
                }
            }
        }
    }

    private fun granted(): Boolean = context.checkSelfPermission(LOCAL_NETWORK_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun resolve(host: String): List<InetAddress> = runCatching { InetAddress.getAllByName(host).toList() }.getOrDefault(emptyList())

    private fun activeVpnRoutes(manager: ConnectivityManager): List<RouteInfo> {
        val network = manager.activeNetwork ?: return emptyList()
        val capabilities = manager.getNetworkCapabilities(network) ?: return emptyList()
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return emptyList()
        return manager.getLinkProperties(network)?.routes.orEmpty()
    }
}

internal fun isLocalHostname(host: String): Boolean {
    val normalized = host.lowercase().trimEnd('.')
    return normalized == "local" || normalized.endsWith(".local")
}

@RequiresApi(37)
private fun LinkAddress.contains(address: InetAddress): Boolean = IpPrefix(this.address, prefixLength).contains(address)

@RequiresApi(37)
private fun List<RouteInfo>.fallsThroughToLan(address: InetAddress): Boolean =
    filter { it.matches(address) }
        .maxByOrNull { it.destination.prefixLength }
        ?.let { it.type == RouteInfo.RTN_THROW } ?: true

@RequiresApi(37)
private fun List<RouteInfo>.mayFallThroughToLan(linkAddresses: List<LinkAddress>): Boolean =
    any { route ->
        route.type == RouteInfo.RTN_THROW &&
            linkAddresses.any { link -> route.matches(link.address) || link.contains(route.destination.address) }
    } || linkAddresses.any { fallsThroughToLan(it.address) }

private val NetworkCapabilities.isPhysicalLan: Boolean
    get() =
        !hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
            !hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
