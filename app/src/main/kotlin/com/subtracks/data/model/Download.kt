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

class DownloadStatusConverter {
    @ColumnTypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String = status.name

    @ColumnTypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.entries.firstOrNull { it.name == value } ?: DownloadStatus.Failed
}
