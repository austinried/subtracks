package com.subtracks.data.db

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtworkSeed
import com.subtracks.data.model.Disc
import com.subtracks.data.model.DownloadStatusConverter
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKindConverter
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.model.UpNextEntry

@Database(
    entities = [
        Source::class,
        SubsonicSource::class,
        Artist::class,
        Album::class,
        Disc::class,
        Playlist::class,
        PlaylistSong::class,
        Song::class,
        QueueEntry::class,
        UpNextEntry::class,
        PlaybackCursor::class,
        ArtworkSeed::class,
        SongDownload::class,
    ],
    version = 18,
    exportSchema = true,
)
@ColumnTypeConverters(QueueKindConverter::class, DownloadStatusConverter::class)
abstract class SubtracksDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    abstract fun sourcesDao(): SourcesDao

    abstract fun queueDao(): QueueDao

    abstract fun artworkSeedDao(): ArtworkSeedDao

    abstract fun downloadDao(): DownloadDao
}
