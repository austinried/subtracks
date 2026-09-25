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

enum class AlbumSort { Name, Artist, Year, Added, Starred }

enum class ArtistSort { Name, AlbumCount, Starred }

enum class PlaylistSort { Name, Added, Updated }

enum class SongSort { Album, Title, Artist, Starred, Added }

enum class LibraryListTab(
    val key: String,
    val defaultSort: String,
    val supportsStarred: Boolean,
) {
    Albums("albums", AlbumSort.Name.name, true),
    Artists("artists", ArtistSort.Name.name, true),
    Playlists("playlists", PlaylistSort.Name.name, false),
    Songs("songs", SongSort.Album.name, true),
}

enum class StarredFilter { Any, Starred, NotStarred }

data class ListQuery(
    val sort: String,
    val descending: Boolean = false,
    val starred: StarredFilter = StarredFilter.Any,
)

private val Context.preferences: DataStore<Preferences> by preferencesDataStore("user_prefs")

class UserPreferences(
    private val store: DataStore<Preferences>,
) {
    fun listQuery(tab: LibraryListTab): Flow<ListQuery> =
        store.data.map { prefs -> decode(prefs[listQueryKey(tab)] ?: legacyQuery(prefs, tab), tab.defaultSort) }

    private fun legacyQuery(
        prefs: Preferences,
        tab: LibraryListTab,
    ): String? {
        if (tab != LibraryListTab.Albums) return null
        val sort = prefs[ALBUM_SORT]?.let { if (it == "RecentlyAdded") "Added" else it } ?: return null
        return "$sort|0|0"
    }

    suspend fun setListQuery(
        tab: LibraryListTab,
        query: ListQuery,
    ) {
        store.edit { prefs -> prefs[listQueryKey(tab)] = encode(query) }
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

    private fun listQueryKey(tab: LibraryListTab) = stringPreferencesKey("list_query_${tab.key}")

    private fun encode(query: ListQuery) = "${query.sort}|${if (query.descending) 1 else 0}|${query.starred.ordinal}"

    private fun decode(
        stored: String?,
        defaultSort: String,
    ): ListQuery {
        val parts = stored?.split("|")
        if (parts == null || parts.size != 3) return ListQuery(defaultSort)
        return ListQuery(
            sort = parts[0].ifEmpty { defaultSort },
            descending = parts[1] == "1",
            starred = StarredFilter.entries.getOrElse(parts[2].toIntOrNull() ?: 0) { StarredFilter.Any },
        )
    }

    private companion object {
        val ALBUM_SORT = stringPreferencesKey("album_sort")
        val MAX_BITRATE = intPreferencesKey("max_bitrate")
        val STREAM_FORMAT = stringPreferencesKey("stream_format")
    }
}

fun createUserPreferences(context: Context): UserPreferences = UserPreferences(context.preferences)
