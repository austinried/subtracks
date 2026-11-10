package com.subtracks.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.subtracks.R
import com.subtracks.data.net.LOCAL_NETWORK_PERMISSION
import com.subtracks.data.net.LocalNetworkAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class LocalNetworkGate internal constructor(
    private val scope: CoroutineScope,
    private val checker: LocalNetworkAccess,
    private val requestPermission: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit,
) {
    fun run(
        address: String?,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
    ) {
        if (address == null) {
            onGranted()
            return
        }
        scope.launch {
            if (checker.requiresPermission(address)) {
                requestPermission(onGranted, onDenied)
            } else {
                onGranted()
            }
        }
    }
}

private class PendingRequest(
    val onGranted: () -> Unit,
    val onDenied: () -> Unit,
)

@Composable
fun rememberLocalNetworkGate(): LocalNetworkGate {
    val checker = koinInject<LocalNetworkAccess>()
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<PendingRequest?>(null) }
    var awaitingSystemPrompt by remember { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val request = pending
            pending = null
            awaitingSystemPrompt = false
            if (request != null) {
                if (granted) request.onGranted() else request.onDenied()
            }
        }

    val gate =
        LocalNetworkGate(scope, checker) { onGranted, onDenied ->
            pending = PendingRequest(onGranted, onDenied)
        }

    val request = pending
    if (request != null && !awaitingSystemPrompt) {
        AlertDialog(
            onDismissRequest = {
                pending = null
                request.onDenied()
            },
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
                    onClick = {
                        pending = null
                        request.onDenied()
                    },
                ) {
                    Text(stringResource(R.string.actions_cancel))
                }
            },
        )
    }
    return gate
}
