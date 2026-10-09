package com.subtracks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Source
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.DEFAULT_FETCH_CONCURRENCY
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sourceRepository: SourceRepository,
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

    val downloadQuality: StateFlow<StreamQuality> =
        userPreferences
            .downloadQuality()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamQuality())

    val downloadOverMetered: StateFlow<Boolean> =
        userPreferences
            .downloadOverMetered()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val syncConcurrency: StateFlow<Int> =
        userPreferences
            .syncConcurrency()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEFAULT_FETCH_CONCURRENCY)

    val scrobbling: StateFlow<Boolean> =
        userPreferences
            .scrobbling()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val offline: StateFlow<Boolean> = sourceRepository.offline

    val verboseLogging: StateFlow<Boolean> =
        userPreferences
            .verboseLogging()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setOfflineMode(enabled: Boolean) {
        sourceRepository.setOfflineMode(enabled)
    }

    fun setVerboseLogging(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setVerboseLogging(enabled) }
    }

    fun selectSource(id: Long) {
        viewModelScope.launch { sourceRepository.selectSource(id) }
    }

    fun setWifiQuality(quality: StreamQuality) {
        viewModelScope.launch { userPreferences.setStreamQuality(NetworkMode.Wifi, quality) }
    }

    fun setMobileQuality(quality: StreamQuality) {
        viewModelScope.launch { userPreferences.setStreamQuality(NetworkMode.Mobile, quality) }
    }

    fun setDownloadQuality(quality: StreamQuality) {
        viewModelScope.launch { userPreferences.setDownloadQuality(quality) }
    }

    fun setDownloadOverMetered(allowed: Boolean) {
        viewModelScope.launch { userPreferences.setDownloadOverMetered(allowed) }
    }

    fun setSyncConcurrency(value: Int) {
        viewModelScope.launch { userPreferences.setSyncConcurrency(value) }
    }

    fun setScrobbling(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setScrobbling(enabled) }
    }
}
