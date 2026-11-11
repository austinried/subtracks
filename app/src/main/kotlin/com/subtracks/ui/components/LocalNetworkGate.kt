package com.subtracks.ui.components

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.subtracks.R
import com.subtracks.data.net.LOCAL_NETWORK_PERMISSION
import com.subtracks.data.net.LocalNetworkAccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class LocalNetworkGate internal constructor(
    private val scope: CoroutineScope,
    private val checker: LocalNetworkAccess,
    private val requestPermission: (address: String, onGranted: () -> Unit, onDenied: () -> Unit) -> Unit,
    private val clearPrompt: () -> Unit,
) {
    private var generation = 0
    private var check: Job? = null

    fun run(
        address: String?,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
        onNotNeeded: () -> Unit = onGranted,
        automatic: Boolean = false,
    ) {
        cancel()
        if (address == null) {
            onNotNeeded()
            return
        }
        val current = generation
        check =
            scope.launch {
                val required =
                    try {
                        checker.requiresPermission(address)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        false
                    }
                if (current != generation) return@launch
                ensureActive()
                if (required && !(automatic && checker.wasDenied(address))) {
                    requestPermission(address, onGranted, onDenied)
                } else {
                    onNotNeeded()
                }
            }
    }

    fun cancel() {
        generation++
        check?.cancel()
        check = null
        clearPrompt()
    }
}

private class PendingRequest(
    val address: String,
    val onGranted: () -> Unit,
    val onDenied: () -> Unit,
)

@Composable
fun rememberLocalNetworkGate(): LocalNetworkGate {
    val checker = koinInject<LocalNetworkAccess>()
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<PendingRequest?>(null) }
    var awaitingSystemPrompt by remember { mutableStateOf(false) }

    fun deny(request: PendingRequest) {
        pending = null
        checker.markDenied(request.address)
        request.onDenied()
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val request = pending
            pending = null
            awaitingSystemPrompt = false
            if (request != null) {
                if (granted) request.onGranted() else deny(request)
            }
        }

    val gate =
        remember(scope, checker) {
            LocalNetworkGate(
                scope = scope,
                checker = checker,
                requestPermission = { address, onGranted, onDenied ->
                    if (pending == null) pending = PendingRequest(address, onGranted, onDenied)
                },
                clearPrompt = {
                    if (!awaitingSystemPrompt) pending = null
                },
            )
        }

    val request = pending
    if (request != null && !awaitingSystemPrompt) {
        AlertDialog(
            onDismissRequest = { deny(request) },
            title = { Text(stringResource(R.string.local_network_permission_title)) },
            text = { Text(stringResource(R.string.local_network_permission_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        awaitingSystemPrompt = true
                        launcher.launch(LOCAL_NETWORK_PERMISSION)
                    },
                ) {
                    Text(stringResource(R.string.local_network_permission_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deny(request) },
                ) {
                    Text(stringResource(R.string.actions_cancel))
                }
            },
        )
    }
    return gate
}

@Composable
internal fun rememberNetworkRevision(): Int {
    val context = LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val notifyChanged = { mainExecutor.execute { revision++ } }
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    notifyChanged()
                }

                override fun onLost(network: Network) {
                    notifyChanged()
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties,
                ) {
                    notifyChanged()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    notifyChanged()
                }
            }
        runCatching { manager?.registerNetworkCallback(NetworkRequest.Builder().build(), callback) }
        onDispose {
            if (manager != null) runCatching { manager.unregisterNetworkCallback(callback) }
        }
    }
    return revision
}
