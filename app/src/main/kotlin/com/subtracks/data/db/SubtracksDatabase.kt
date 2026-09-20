package com.subtracks.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.AppSettings
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.SearchIndex
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource

@Database(
    entities = [
        Source::class,
        SubsonicSource::class,
        AppSettings::class,
        Artist::class,
        Album::class,
        Playlist::class,
        PlaylistSong::class,
        Song::class,
        SearchIndex::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class SubtracksDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    abstract fun sourcesDao(): SourcesDao

    abstract fun searchDao(): SearchDao
}
