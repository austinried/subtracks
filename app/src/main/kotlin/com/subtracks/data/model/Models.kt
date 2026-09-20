package com.subtracks.data.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Fts5
import androidx.room3.FtsOptions
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val isActive: Boolean = false,
    val createdAt: Long,
)

@Entity(
    tableName = "subsonic_sources",
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId")],
)
data class SubsonicSource(
    @PrimaryKey val sourceId: Long,
    val username: String,
    val password: String,
    val useTokenAuth: Boolean = true,
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Long = 1,
    val maxBitrateWifi: Int = 0,
    val maxBitrateMobile: Int = 192,
    val streamFormat: String? = null,
)

@Entity(
    tableName = "artists",
    primaryKeys = ["sourceId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId")],
)
data class Artist(
    val sourceId: Long,
    val id: String,
    val name: String,
    val albumCount: Long,
    val starred: Long?,
)

@Entity(
    tableName = "albums",
    primaryKeys = ["sourceId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId"), Index("sourceId", "artistId")],
)
data class Album(
    val sourceId: Long,
    val id: String,
    val artistId: String?,
    val name: String,
    val albumArtist: String?,
    val created: Long,
    val coverArt: String?,
    val genre: String?,
    val year: Long?,
    val starred: Long?,
    val songCount: Long,
    val frequentRank: Long?,
    val recentRank: Long?,
)

@Entity(
    tableName = "playlists",
    primaryKeys = ["sourceId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId")],
)
data class Playlist(
    val sourceId: Long,
    val id: String,
    val name: String,
    val comment: String?,
    val coverArt: String?,
    val songCount: Long,
    val created: Long,
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["sourceId", "playlistId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId", "playlistId"), Index("sourceId", "songId")],
)
data class PlaylistSong(
    val sourceId: Long,
    val playlistId: String,
    val songId: String,
    val position: Long,
)

@Entity(
    tableName = "songs",
    primaryKeys = ["sourceId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId", "albumId"), Index("sourceId", "artistId")],
)
data class Song(
    val sourceId: Long,
    val id: String,
    val albumId: String?,
    val artistId: String?,
    val title: String,
    val album: String?,
    val artist: String?,
    val duration: Long?,
    val track: Long?,
    val disc: Long?,
    val starred: Long?,
    val genre: String?,
)

@Entity(tableName = "search_index")
@Fts5(tokenizer = FtsOptions.TOKENIZER_TRIGRAM, notIndexed = ["sourceId", "type", "itemId"])
data class SearchIndex(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val rowId: Long = 0,
    val sourceId: String,
    val type: String,
    val itemId: String,
    val title: String,
)
