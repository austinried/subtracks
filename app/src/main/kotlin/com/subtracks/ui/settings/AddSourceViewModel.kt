package com.subtracks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddSourceState(
    val name: String = "",
    val address: String = "",
    val username: String = "",
    val password: String = "",
    val useTokenAuth: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val nameError: Boolean = false,
    val addressError: Boolean = false,
)

internal fun AddSourceState.validated(): AddSourceState =
    copy(
        nameError = name.isBlank(),
        addressError = address.isBlank(),
        message = null,
        isError = false,
    )

class AddSourceViewModel(
    private val sourceRepository: SourceRepository,
    private val syncManager: SyncManager,
) : ViewModel() {
    private val _state = MutableStateFlow(AddSourceState())
    val state: StateFlow<AddSourceState> = _state

    fun setName(value: String) = _state.update { it.copy(name = value, nameError = false) }

    fun setAddress(value: String) = _state.update { it.copy(address = value, addressError = false) }

    fun setUsername(value: String) = _state.update { it.copy(username = value) }

    fun setPassword(value: String) = _state.update { it.copy(password = value) }

    fun setTokenAuth(value: Boolean) = _state.update { it.copy(useTokenAuth = value) }

    fun testConnection() {
        _state.update { it.copy(busy = true, message = null) }
        val current = _state.value
        viewModelScope.launch {
            val result = sourceRepository.ping(current.address, current.username, current.password, current.useTokenAuth)
            _state.update { state ->
                val fellBack = result.getOrDefault(false)
                state.copy(
                    busy = false,
                    useTokenAuth = if (fellBack) false else state.useTokenAuth,
                    message =
                        result.fold(
                            { if (fellBack) "Server does not support token auth; using the password instead" else "Connection OK" },
                            { error -> "Failed: ${error.message}" },
                        ),
                    isError = result.isFailure,
                )
            }
        }
    }

    fun save(onSaved: () -> Unit) {
        val validated = _state.value.validated()
        if (validated.nameError || validated.addressError) {
            _state.value = validated
            return
        }
        _state.update { it.copy(busy = true, message = null) }
        val current = _state.value
        viewModelScope.launch {
            runCatching {
                sourceRepository.addSource(
                    name = current.name,
                    address = current.address,
                    username = current.username,
                    password = current.password,
                    useTokenAuth = current.useTokenAuth,
                )
            }.fold(
                onSuccess = {
                    syncManager.requestSync()
                    _state.update { it.copy(busy = false) }
                    onSaved()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(busy = false, message = "Could not save: ${error.message}", isError = true)
                    }
                },
            )
        }
    }
}
