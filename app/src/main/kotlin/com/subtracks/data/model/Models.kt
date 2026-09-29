package com.subtracks.data.model

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverter
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Fts5
import androidx.room3.FtsOptions
import androidx.room3.Ignore
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
    indices = [
        Index("sourceId"),
        Index(name = "index_artists_name", value = ["sourceId", "name", "id"]),
        Index(
            name = "index_artists_albumCount",
            value = ["sourceId", "albumCount", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
        Index(
            name = "index_artists_starred",
            value = ["sourceId", "starred", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
    ],
)
data class Artist(
    val sourceId: Long,
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val albumCount: Long,
    val starred: Long?,
    val coverArt: String? = null,
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
    indices = [
        Index("sourceId"),
        Index("sourceId", "artistId"),
        Index(name = "index_albums_name", value = ["sourceId", "name", "id"]),
        Index(name = "index_albums_artist", value = ["sourceId", "albumArtist", "year", "name", "id"]),
        Index(
            name = "index_albums_year",
            value = ["sourceId", "year", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
        Index(
            name = "index_albums_added",
            value = ["sourceId", "created", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
        Index(
            name = "index_albums_starred",
            value = ["sourceId", "starred", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
    ],
)
data class Album(
    val sourceId: Long,
    val id: String,
    val artistId: String?,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val albumArtist: String?,
    val created: Long,
    val coverArt: String?,
    val genre: String?,
    val year: Long?,
    val starred: Long?,
    val songCount: Long,
    @Ignore val discTitles: Map<Long, String> = emptyMap(),
)

@Entity(
    tableName = "discs",
    primaryKeys = ["sourceId", "albumId", "disc"],
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["sourceId", "id"],
            childColumns = ["sourceId", "albumId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId", "albumId")],
)
data class Disc(
    val sourceId: Long,
    val albumId: String,
    val disc: Long,
    val title: String,
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
    indices = [
        Index("sourceId"),
        Index(name = "index_playlists_name", value = ["sourceId", "name", "id"]),
        Index(
            name = "index_playlists_added",
            value = ["sourceId", "created", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
        Index(
            name = "index_playlists_updated",
            value = ["sourceId", "changed", "name", "id"],
            orders = [Index.Order.ASC, Index.Order.DESC, Index.Order.ASC, Index.Order.ASC],
        ),
    ],
)
data class Playlist(
    val sourceId: Long,
    val id: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val comment: String?,
    val coverArt: String?,
    val songCount: Long,
    val created: Long,
    @ColumnInfo(defaultValue = "0") val changed: Long = 0,
    @ColumnInfo(defaultValue = "0") val duration: Long = 0,
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
    indices = [
        Index("sourceId", "artistId"),
        Index(
            name = "index_songs_sourceId_albumId_order",
            value = ["sourceId", "albumId", "disc", "track", "id"],
        ),
    ],
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
    @ColumnInfo(defaultValue = "0") val created: Long = 0,
)

sealed interface SongItem {
    val song: Song
    val coverArt: String?
    val playlistPosition: Long? get() = null
}

data class AlbumSongItem(
    @Embedded override val song: Song,
    override val coverArt: String?,
) : SongItem

data class PlaylistSongItem(
    @Embedded override val song: Song,
    override val coverArt: String?,
    val position: Long,
) : SongItem {
    override val playlistPosition: Long get() = position
}

data class DiscKey(
    val albumId: String,
    val disc: Long,
)

enum class QueueKind { Playlist, Album, Song, Artist }

@Entity(
    tableName = "queue_entries",
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId"), Index("position")],
)
data class QueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val position: Long,
    val sourceId: Long,
    val kind: QueueKind,
    val refId: String,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null,
) {
    val offset: Long get() = rangeStart ?: 0

    val count: Long? get() = rangeEnd?.let { (it - offset + 1).coerceAtLeast(0) }
}

@Entity(
    tableName = "up_next_entries",
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId"), Index("position")],
)
data class UpNextEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val position: Long,
    val sourceId: Long,
    val kind: QueueKind,
    val refId: String,
    val rangeStart: Long? = null,
    val rangeEnd: Long? = null,
) {
    val offset: Long get() = rangeStart ?: 0

    val count: Long? get() = rangeEnd?.let { (it - offset + 1).coerceAtLeast(0) }

    fun toQueueEntry(): QueueEntry = QueueEntry(id, position, sourceId, kind, refId, rangeStart, rangeEnd)

    companion object {
        fun from(entry: QueueEntry) =
            UpNextEntry(entry.id, entry.position, entry.sourceId, entry.kind, entry.refId, entry.rangeStart, entry.rangeEnd)
    }
}

@Entity(tableName = "playback_cursor")
data class PlaybackCursor(
    @PrimaryKey val id: Long = 1,
    val queuePosition: Long,
    @ColumnInfo(defaultValue = "0") val shuffleEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0") val repeatMode: Int = 0,
    @ColumnInfo(defaultValue = "0") val shuffleSeed: Long = 0,
    @ColumnInfo(defaultValue = "0") val shuffleSize: Long = 0,
    @ColumnInfo(defaultValue = "0") val positionMs: Long = 0,
    @ColumnInfo(defaultValue = "0") val upNextAnchor: Long = 0,
)

@Entity(
    tableName = "artwork_seeds",
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
data class ArtworkSeed(
    @PrimaryKey val cacheKey: String,
    val primary: Int,
    val secondary: Int?,
    val nameBusy: Boolean? = null,
    val sourceId: Long = 0,
    @ColumnInfo(defaultValue = "0") val storedAt: Long = 0,
)

@Entity(tableName = "album_search")
@Fts5(contentEntity = Album::class, tokenizer = FtsOptions.TOKENIZER_TRIGRAM)
data class AlbumSearch(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long = 0,
    val name: String,
    val albumArtist: String?,
)

@Entity(tableName = "artist_search")
@Fts5(contentEntity = Artist::class, tokenizer = FtsOptions.TOKENIZER_TRIGRAM)
data class ArtistSearch(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long = 0,
    val name: String,
)

@Entity(tableName = "playlist_search")
@Fts5(contentEntity = Playlist::class, tokenizer = FtsOptions.TOKENIZER_TRIGRAM)
data class PlaylistSearch(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long = 0,
    val name: String,
)

class QueueKindConverter {
    @ColumnTypeConverter
    fun fromQueueKind(kind: QueueKind): String = kind.name

    @ColumnTypeConverter
    fun toQueueKind(value: String): QueueKind = QueueKind.entries.firstOrNull { it.name == value } ?: QueueKind.Song
}
