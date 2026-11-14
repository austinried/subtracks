package com.subtracks.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.subtracks.data.net.LOCAL_NETWORK_PERMISSION
import com.subtracks.data.net.LocalNetworkAccess
import com.subtracks.data.prefs.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private class PendingLocalNetworkAction(
    val onGranted: () -> Unit,
    val onDenied: () -> Unit,
)

@Composable
internal fun rememberLocalNetworkRequest(): (String, () -> Unit, () -> Unit) -> Unit {
    val access = koinInject<LocalNetworkAccess>()
    val preferences = koinInject<UserPreferences>()
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<PendingLocalNetworkAction?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val action = pending
            pending = null
            if (granted) action?.onGranted?.invoke() else action?.onDenied?.invoke()
        }

    return { address, onGranted, onDenied ->
        scope.launch {
            val required =
                try {
                    !preferences.offlineMode().first() && access.requiresPermission(address)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    false
                }
            if (required) {
                pending = PendingLocalNetworkAction(onGranted, onDenied)
                launcher.launch(LOCAL_NETWORK_PERMISSION)
            } else {
                onGranted()
            }
        }
    }
}
