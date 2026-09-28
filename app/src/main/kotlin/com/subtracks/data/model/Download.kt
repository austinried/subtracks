package com.subtracks.data.model

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverter
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

enum class DownloadStatus { Queued, Running, Completed, Failed }

@Entity(
    tableName = "song_downloads",
    primaryKeys = ["sourceId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["sourceId", "id"],
            childColumns = ["sourceId", "songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sourceId"), Index("engineId")],
)
data class SongDownload(
    val sourceId: Long,
    val songId: String,
    val status: DownloadStatus,
    val engineId: Long? = null,
    @ColumnInfo(defaultValue = "0") val bytes: Long = 0,
    @ColumnInfo(defaultValue = "0") val total: Long = 0,
    val error: String? = null,
) {
    val progress: Float?
        get() = total.takeIf { it > 0 }?.let { (bytes.toFloat() / it).coerceIn(0f, 1f) }
}

enum class DownloadList { Album, Playlist, Artist }

enum class BulkDownloadAction { Download, Cancel, Delete }

data class ListDownloadStatus(
    val total: Long = 0,
    val downloaded: Long = 0,
    val downloading: Long = 0,
) {
    val complete: Boolean get() = total > 0 && downloaded == total
}

data class EntityDownloadStatus(
    val id: String,
    val total: Long = 0,
    val downloaded: Long = 0,
    val downloading: Long = 0,
) {
    fun toListStatus(): ListDownloadStatus = ListDownloadStatus(total, downloaded, downloading)
}

data class DownloadedSong(
    val songId: String,
    val title: String,
    val albumId: String?,
    val albumName: String?,
    val artistId: String?,
    val artistName: String?,
    val status: DownloadStatus,
    val size: Long = 0,
)

data class DownloadArtwork(
    val sourceId: Long,
    val albumId: String?,
    val artistId: String?,
    val albumRow: String?,
    val artistRow: String?,
    val albumCoverArt: String?,
    val artistCoverArt: String?,
) {
    val hasMissingLibraryRow: Boolean
        get() = (albumId != null && albumRow == null) || (artistId != null && artistRow == null)
}

class DownloadStatusConverter {
    @ColumnTypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @ColumnTypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.entries.firstOrNull { it.name == value } ?: DownloadStatus.Failed
}
