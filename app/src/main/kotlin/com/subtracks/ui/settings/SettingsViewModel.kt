package com.subtracks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Source
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.DEFAULT_SYNC_CONCURRENCY
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sourceRepository: SourceRepository,
    private val downloadRepository: DownloadRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {
    val sources: StateFlow<List<Source>> =
        sourceRepository.sources().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSourceId: StateFlow<Long?> =
        sourceRepository.activeSourceId().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val wifiQuality: StateFlow<StreamQuality> =
        userPreferences
            .streamQuality(NetworkMode.Wifi)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamQuality())

    val mobileQuality: StateFlow<StreamQuality> =
        userPreferences
            .streamQuality(NetworkMode.Mobile)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamQuality())

    val syncConcurrency: StateFlow<Int> =
        userPreferences
            .syncConcurrency()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_SYNC_CONCURRENCY)

    fun selectSource(id: Long) {
        viewModelScope.launch { sourceRepository.selectSource(id) }
    }

    fun deleteSource(id: Long) {
        viewModelScope.launch {
            downloadRepository.removeSource(id)
            sourceRepository.deleteSource(id)
        }
    }

    fun setWifiQuality(quality: StreamQuality) {
        viewModelScope.launch { userPreferences.setStreamQuality(NetworkMode.Wifi, quality) }
    }

    fun setMobileQuality(quality: StreamQuality) {
        viewModelScope.launch { userPreferences.setStreamQuality(NetworkMode.Mobile, quality) }
    }

    fun setSyncConcurrency(value: Int) {
        viewModelScope.launch { userPreferences.setSyncConcurrency(value) }
    }
}
