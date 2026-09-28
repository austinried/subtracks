package com.subtracks.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.source.DEFAULT_FETCH_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class AlbumSort { Name, Artist, Year, Added, Starred }

enum class ArtistSort { Name, AlbumCount, Starred }

enum class PlaylistSort { Name, Added, Updated }

enum class LibraryListTab(
    val key: String,
    val defaultSort: String,
    val supportsStarred: Boolean,
) {
    Albums("albums", AlbumSort.Name.name, true),
    Artists("artists", ArtistSort.Name.name, true),
    Playlists("playlists", PlaylistSort.Name.name, false),
}

enum class StarredFilter { Any, Starred, NotStarred }

data class ListQuery(
    val sort: String,
    val descending: Boolean = false,
    val starred: StarredFilter = StarredFilter.Any,
    val downloaded: Boolean = false,
)

data class StreamQuality(
    val maxBitrate: Int = 0,
    val format: String? = null,
) {
    val transcodes: Boolean get() = maxBitrate > 0 || !format.isNullOrEmpty()
}

private val Context.preferences: DataStore<Preferences> by preferencesDataStore("user_prefs")

data class ArtworkSeedValue(
    val cacheKey: String,
    val primary: Int,
    val secondary: Int?,
)

class UserPreferences(
    private val store: DataStore<Preferences>,
) {
    fun listQuery(tab: LibraryListTab): Flow<ListQuery> = store.data.map { prefs -> decode(prefs[listQueryKey(tab)], tab.defaultSort) }

    suspend fun setListQuery(
        tab: LibraryListTab,
        query: ListQuery,
    ) {
        store.edit { prefs -> prefs[listQueryKey(tab)] = encode(query) }
    }

    fun streamQuality(mode: NetworkMode): Flow<StreamQuality> =
        store.data.map { prefs ->
            prefs[streamQualityKey(mode)]?.let(::decodeStreamQuality) ?: defaultStreamQuality(mode)
        }

    suspend fun setStreamQuality(
        mode: NetworkMode,
        quality: StreamQuality,
    ) {
        store.edit { prefs -> prefs[streamQualityKey(mode)] = encode(quality) }
    }

    fun downloadQuality(): Flow<StreamQuality> =
        store.data.map { prefs -> prefs[downloadQualityKey]?.let(::decodeStreamQuality) ?: StreamQuality() }

    suspend fun setDownloadQuality(quality: StreamQuality) {
        store.edit { prefs -> prefs[downloadQualityKey] = encode(quality) }
    }

    private fun streamQualityKey(mode: NetworkMode) = stringPreferencesKey("stream_quality_${mode.key}")

    private val downloadQualityKey = stringPreferencesKey("download_quality")

    fun downloadOverMetered(): Flow<Boolean> = store.data.map { prefs -> prefs[downloadOverMeteredKey] ?: false }

    suspend fun setDownloadOverMetered(allowed: Boolean) {
        store.edit { prefs -> prefs[downloadOverMeteredKey] = allowed }
    }

    private val downloadOverMeteredKey = booleanPreferencesKey("download_over_metered")

    fun syncConcurrency(): Flow<Int> = store.data.map { prefs -> (prefs[syncConcurrencyKey] ?: DEFAULT_FETCH_CONCURRENCY).coerceAtLeast(1) }

    suspend fun setSyncConcurrency(value: Int) {
        store.edit { prefs -> prefs[syncConcurrencyKey] = value.coerceAtLeast(1) }
    }

    private val syncConcurrencyKey = intPreferencesKey("sync_concurrency")

    fun scrobbling(): Flow<Boolean> = store.data.map { prefs -> prefs[scrobblingKey] ?: true }

    suspend fun setScrobbling(enabled: Boolean) {
        store.edit { prefs -> prefs[scrobblingKey] = enabled }
    }

    private val scrobblingKey = booleanPreferencesKey("scrobbling")

    suspend fun lastSeed(): ArtworkSeedValue? {
        val prefs = store.data.first()
        val cacheKey = prefs[lastSeedKey] ?: return null
        val primary = prefs[lastSeedPrimary] ?: return null
        return ArtworkSeedValue(cacheKey, primary, prefs[lastSeedSecondary])
    }

    suspend fun setLastSeed(value: ArtworkSeedValue) {
        store.edit { prefs ->
            prefs[lastSeedKey] = value.cacheKey
            prefs[lastSeedPrimary] = value.primary
            if (value.secondary != null) prefs[lastSeedSecondary] = value.secondary else prefs.remove(lastSeedSecondary)
        }
    }

    private val lastSeedKey = stringPreferencesKey("last_seed_key")
    private val lastSeedPrimary = intPreferencesKey("last_seed_primary")
    private val lastSeedSecondary = intPreferencesKey("last_seed_secondary")

    private fun listQueryKey(tab: LibraryListTab) = stringPreferencesKey("list_query_${tab.key}")

    private fun encode(query: ListQuery) =
        "${query.sort}|${if (query.descending) 1 else 0}|${query.starred.ordinal}|${if (query.downloaded) 1 else 0}"

    private fun encode(quality: StreamQuality) = "${quality.maxBitrate}|${quality.format.orEmpty()}"

    private fun decodeStreamQuality(stored: String): StreamQuality {
        val parts = stored.split("|")
        return StreamQuality(
            maxBitrate = parts.getOrNull(0)?.toIntOrNull() ?: 0,
            format = parts.getOrNull(1)?.takeIf { it.isNotEmpty() },
        )
    }

    private fun defaultStreamQuality(mode: NetworkMode): StreamQuality =
        when (mode) {
            NetworkMode.Wifi -> StreamQuality()
            NetworkMode.Mobile -> StreamQuality(maxBitrate = 192, format = "mp3")
        }

    private fun decode(
        stored: String?,
        defaultSort: String,
    ): ListQuery {
        val parts = stored?.split("|")
        if (parts == null || parts.size < 3) return ListQuery(defaultSort)
        return ListQuery(
            sort = parts[0].ifEmpty { defaultSort },
            descending = parts[1] == "1",
            starred = StarredFilter.entries.getOrElse(parts[2].toIntOrNull() ?: 0) { StarredFilter.Any },
            downloaded = parts.getOrNull(3) == "1",
        )
    }
}

fun createUserPreferences(context: Context): UserPreferences = UserPreferences(context.preferences)
