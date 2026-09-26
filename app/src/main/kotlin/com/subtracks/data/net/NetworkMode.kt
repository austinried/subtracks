package com.subtracks.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkMode(
    val key: String,
) {
    Wifi("wifi"),
    Mobile("mobile"),
}

internal fun networkModeFor(
    metered: Boolean?,
    last: NetworkMode,
): NetworkMode =
    when (metered) {
        true -> NetworkMode.Mobile
        false -> NetworkMode.Wifi
        null -> last
    }

fun networkMode(context: Context): Flow<NetworkMode> {
    val connectivity = context.applicationContext.getSystemService(ConnectivityManager::class.java)
    return callbackFlow {
        var last = NetworkMode.Wifi

        fun push() {
            val metered = connectivity?.let { if (it.activeNetwork == null) null else it.isActiveNetworkMetered }
            last = networkModeFor(metered, last)
            this@callbackFlow.trySend(last)
        }
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = push()

                override fun onLost(network: Network) = push()

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities,
                ) = push()
            }
        val registered = runCatching { connectivity?.registerDefaultNetworkCallback(callback) }.isSuccess
        push()
        awaitClose { if (registered) runCatching { connectivity?.unregisterNetworkCallback(callback) } }
    }.distinctUntilChanged()
}
