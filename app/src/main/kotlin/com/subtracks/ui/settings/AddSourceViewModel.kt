package com.subtracks.ui.settings

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.R
import com.subtracks.UiException
import com.subtracks.data.repo.DownloadRepository
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
    val isEditing: Boolean = false,
    val canDelete: Boolean = false,
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
    private val downloadRepository: DownloadRepository,
    private val sourceId: Long? = null,
    private val resources: Resources,
) : ViewModel() {
    private val _state = MutableStateFlow(AddSourceState(isEditing = sourceId != null))
    val state: StateFlow<AddSourceState> = _state

    init {
        sourceId?.let { id ->
            viewModelScope.launch {
                val config = sourceRepository.sourceConfigOnce(id) ?: return@launch
                _state.update {
                    it.copy(
                        name = config.name,
                        address = config.address,
                        username = config.username,
                        password = config.password,
                        useTokenAuth = config.useTokenAuth,
                    )
                }
            }
        }
        viewModelScope.launch {
            sourceRepository.sources().collect { sources ->
                val active = sources.any { it.id == sourceId && it.isActive }
                _state.update { it.copy(canDelete = !active) }
            }
        }
    }

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
                            {
                                if (fellBack) {
                                    resources.getString(
                                        R.string.connection_token_fallback,
                                    )
                                } else {
                                    resources.getString(R.string.connection_ok)
                                }
                            },
                            { error ->
                                (error as? UiException)?.uiMessage?.resolve(resources)
                                    ?: resources.getString(R.string.connection_failed, error.message)
                            },
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
                val id = sourceId
                if (id == null) {
                    sourceRepository.addSource(
                        name = current.name,
                        address = current.address,
                        username = current.username,
                        password = current.password,
                        useTokenAuth = current.useTokenAuth,
                    )
                } else {
                    sourceRepository.updateSource(
                        id = id,
                        name = current.name,
                        address = current.address,
                        username = current.username,
                        password = current.password,
                        useTokenAuth = current.useTokenAuth,
                    )
                }
            }.fold(
                onSuccess = {
                    syncManager.requestSync()
                    _state.update { it.copy(busy = false) }
                    onSaved()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(busy = false, message = resources.getString(R.string.save_failed, error.message), isError = true)
                    }
                },
            )
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = sourceId ?: return
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            if (!sourceRepository.deleteSource(id)) {
                _state.update {
                    it.copy(busy = false, message = resources.getString(R.string.active_server_delete_error), isError = true)
                }
                return@launch
            }
            downloadRepository.removeSource(id)
            _state.update { it.copy(busy = false) }
            onDeleted()
        }
    }
}
