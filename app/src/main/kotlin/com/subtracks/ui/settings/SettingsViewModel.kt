package com.subtracks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Source
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sourceRepository: SourceRepository,
) : ViewModel() {
    val sources: StateFlow<List<Source>> =
        sourceRepository.sources().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSourceId: StateFlow<Long?> =
        sourceRepository.activeSourceId().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectSource(id: Long) {
        viewModelScope.launch { sourceRepository.selectSource(id) }
    }

    fun deleteSource(id: Long) {
        viewModelScope.launch { sourceRepository.deleteSource(id) }
    }
}
