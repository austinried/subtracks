package com.subtracks.data.net

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

internal const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

private const val LOCAL_NETWORK_PERMISSION_SDK = 37

class LocalNetworkAccess(
    private val context: Context,
) {
    suspend fun requiresPermission(address: String): Boolean {
        if (Build.VERSION.SDK_INT < LOCAL_NETWORK_PERMISSION_SDK || granted()) return false
        val host = serverHost(address) ?: return false
        val linkAddresses = currentLinkAddresses()
        if (linkAddresses.isEmpty()) return false
        return withContext(Dispatchers.IO) {
            val resolved = runCatching { InetAddress.getAllByName(host).toList() }.getOrDefault(emptyList())
            resolved.any { candidate ->
                linkAddresses.any { candidate.isWithinPrefix(it.address, it.prefixLength) }
            }
        }
    }

    private fun granted(): Boolean =
        ContextCompat.checkSelfPermission(context, LOCAL_NETWORK_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun currentLinkAddresses(): List<LinkAddress> {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return emptyList()
        val network = manager.activeNetwork ?: return emptyList()
        return manager.getLinkProperties(network)?.linkAddresses.orEmpty()
    }
}

internal fun InetAddress.isWithinPrefix(
    prefix: InetAddress,
    prefixLength: Int,
): Boolean {
    val own = address
    val network = prefix.address
    if (own.size != network.size || prefixLength !in 0..(own.size * 8)) return false

    val wholeBytes = prefixLength / 8
    for (index in 0 until wholeBytes) {
        if (own[index] != network[index]) return false
    }

    val remainingBits = prefixLength % 8
    if (remainingBits == 0) return true
    val mask = (0xFF shl (8 - remainingBits)) and 0xFF
    return (own[wholeBytes].toInt() and mask) == (network[wholeBytes].toInt() and mask)
}
