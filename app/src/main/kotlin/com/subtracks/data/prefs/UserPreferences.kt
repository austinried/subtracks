package com.subtracks.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AlbumSort { Name, Artist, Year, RecentlyAdded }

private val Context.preferences: DataStore<Preferences> by preferencesDataStore("user_prefs")

class UserPreferences(
    private val store: DataStore<Preferences>,
) {
    val albumSort: Flow<AlbumSort> =
        store.data.map { prefs ->
            prefs[ALBUM_SORT]?.let { stored -> AlbumSort.entries.firstOrNull { it.name == stored } } ?: AlbumSort.Name
        }

    suspend fun setAlbumSort(sort: AlbumSort) {
        store.edit { prefs -> prefs[ALBUM_SORT] = sort.name }
    }

    val maxBitrate: Flow<Int> = store.data.map { prefs -> prefs[MAX_BITRATE] ?: 0 }

    suspend fun setMaxBitrate(kbps: Int) {
        store.edit { prefs -> prefs[MAX_BITRATE] = kbps }
    }

    val streamFormat: Flow<String?> = store.data.map { prefs -> prefs[STREAM_FORMAT] }

    suspend fun setStreamFormat(format: String?) {
        store.edit { prefs ->
            if (format.isNullOrEmpty()) prefs.remove(STREAM_FORMAT) else prefs[STREAM_FORMAT] = format
        }
    }

    private companion object {
        val ALBUM_SORT = stringPreferencesKey("album_sort")
        val MAX_BITRATE = intPreferencesKey("max_bitrate")
        val STREAM_FORMAT = stringPreferencesKey("stream_format")
    }
}

fun createUserPreferences(context: Context): UserPreferences = UserPreferences(context.preferences)
