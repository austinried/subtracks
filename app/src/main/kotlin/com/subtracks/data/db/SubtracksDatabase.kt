package com.subtracks.data.db

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtworkSeed
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKindConverter
import com.subtracks.data.model.SearchIndex
import com.subtracks.data.model.ShuffleOrder
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource

@Database(
    entities = [
        Source::class,
        SubsonicSource::class,
        Artist::class,
        Album::class,
        Playlist::class,
        PlaylistSong::class,
        Song::class,
        SearchIndex::class,
        QueueEntry::class,
        PlaybackCursor::class,
        ShuffleOrder::class,
        ArtworkSeed::class,
    ],
    version = 6,
    exportSchema = true,
)
@ColumnTypeConverters(QueueKindConverter::class)
abstract class SubtracksDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    abstract fun sourcesDao(): SourcesDao

    abstract fun searchDao(): SearchDao

    abstract fun queueDao(): QueueDao

    abstract fun artworkSeedDao(): ArtworkSeedDao
}
