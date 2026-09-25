package com.subtracks.data.model

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverter
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
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
    indices = [Index("sourceId")],
)
data class Artist(
    val sourceId: Long,
    val id: String,
    val name: String,
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
    @ColumnInfo(defaultValue = "0") val created: Long = 0,
)

data class SongListItem(
    @Embedded val song: Song,
    val coverArt: String?,
)

data class DiscKey(
    val albumId: String,
    val disc: Long,
)

enum class QueueKind { Playlist, Album, Song, Songs }

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

@Entity(tableName = "playback_cursor")
data class PlaybackCursor(
    @PrimaryKey val id: Long = 1,
    val queuePosition: Long,
    @ColumnInfo(defaultValue = "0") val shuffleEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "0") val repeatMode: Int = 0,
    @ColumnInfo(defaultValue = "0") val shuffleSeed: Long = 0,
    @ColumnInfo(defaultValue = "0") val positionMs: Long = 0,
)

@Entity(tableName = "shuffle_order")
data class ShuffleOrder(
    @PrimaryKey val sequence: Long,
    val flatPosition: Long,
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

class QueueKindConverter {
    @ColumnTypeConverter
    fun fromQueueKind(kind: QueueKind): String = kind.name

    @ColumnTypeConverter
    fun toQueueKind(value: String): QueueKind = QueueKind.valueOf(value)
}
