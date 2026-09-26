package com.subtracks.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

enum class NetworkMode(
    val key: String,
) {
    Wifi("wifi"),
    Mobile("mobile"),
}

class NetworkMonitor(
    context: Context,
) {
    private val connectivity = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    val mode: Flow<NetworkMode> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) = push()

                    override fun onLost(network: Network) = push()

                    override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities: NetworkCapabilities,
                    ) = push()

                    private fun push() {
                        this@callbackFlow.trySend(current())
                    }
                }
            val registered = runCatching { connectivity?.registerDefaultNetworkCallback(callback) }.isSuccess
            this@callbackFlow.trySend(current())
            awaitClose { if (registered) runCatching { connectivity?.unregisterNetworkCallback(callback) } }
        }.distinctUntilChanged().conflate()

    private fun current(): NetworkMode = if (connectivity?.isActiveNetworkMetered == true) NetworkMode.Mobile else NetworkMode.Wifi
}
