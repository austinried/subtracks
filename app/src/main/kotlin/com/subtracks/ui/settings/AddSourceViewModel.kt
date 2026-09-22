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
)

class AddSourceViewModel(
    private val sourceRepository: SourceRepository,
    private val syncManager: SyncManager,
) : ViewModel() {
    private val _state = MutableStateFlow(AddSourceState())
    val state: StateFlow<AddSourceState> = _state

    fun setName(value: String) = _state.update { it.copy(name = value) }

    fun setAddress(value: String) = _state.update { it.copy(address = value) }

    fun setUsername(value: String) = _state.update { it.copy(username = value) }

    fun setPassword(value: String) = _state.update { it.copy(password = value) }

    fun setTokenAuth(value: Boolean) = _state.update { it.copy(useTokenAuth = value) }

    fun testConnection() {
        _state.update { it.copy(busy = true, message = null) }
        val current = _state.value
        viewModelScope.launch {
            val result = sourceRepository.ping(current.address, current.username, current.password, current.useTokenAuth)
            _state.update {
                it.copy(
                    busy = false,
                    message = result.fold({ "Connection OK" }, { error -> "Failed: ${error.message}" }),
                    isError = result.isFailure,
                )
            }
        }
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.name.isBlank() || current.address.isBlank()) {
            _state.update { it.copy(message = "Name and address are required", isError = true) }
            return
        }
        _state.update { it.copy(busy = true, message = null) }
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
