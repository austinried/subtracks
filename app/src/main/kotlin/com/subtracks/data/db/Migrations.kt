package com.subtracks.data.db

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("DROP TABLE IF EXISTS `app_settings`")
            connection.execSQL("ALTER TABLE `artists` ADD COLUMN `coverArt` TEXT")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `queue_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`position` INTEGER NOT NULL, `sourceId` INTEGER NOT NULL, `kind` TEXT NOT NULL, " +
                    "`refId` TEXT NOT NULL, `rangeStart` INTEGER, `rangeEnd` INTEGER, " +
                    "FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_queue_entries_sourceId` ON `queue_entries` (`sourceId`)",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_queue_entries_position` ON `queue_entries` (`position`)",
            )
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `playback_cursor` (`id` INTEGER NOT NULL, " +
                    "`queuePosition` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            )
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `artwork_seeds` (`cacheKey` TEXT NOT NULL, " +
                    "`primary` INTEGER NOT NULL, `secondary` INTEGER, " +
                    "PRIMARY KEY(`cacheKey`))",
            )
        }
    }

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `playlists` ADD COLUMN `duration` INTEGER NOT NULL DEFAULT 0")
        }
    }

val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `playback_cursor` ADD COLUMN `shuffleEnabled` INTEGER NOT NULL DEFAULT 0")
            connection.execSQL("ALTER TABLE `playback_cursor` ADD COLUMN `repeatMode` INTEGER NOT NULL DEFAULT 0")
            connection.execSQL("ALTER TABLE `playback_cursor` ADD COLUMN `shuffleSeed` INTEGER NOT NULL DEFAULT 0")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `shuffle_order` (`sequence` INTEGER NOT NULL, " +
                    "`flatPosition` INTEGER NOT NULL, PRIMARY KEY(`sequence`))",
            )
        }
    }

val MIGRATION_6_7 =
    object : Migration(6, 7) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `playback_cursor` ADD COLUMN `positionMs` INTEGER NOT NULL DEFAULT 0")
        }
    }

val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `playlists` ADD COLUMN `changed` INTEGER NOT NULL DEFAULT 0")
        }
    }

val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `songs` ADD COLUMN `created` INTEGER NOT NULL DEFAULT 0")
        }
    }

val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("DROP TABLE IF EXISTS `search_index`")
        }
    }

val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `discs` (`sourceId` INTEGER NOT NULL, `albumId` TEXT NOT NULL, " +
                    "`disc` INTEGER NOT NULL, `title` TEXT NOT NULL, PRIMARY KEY(`sourceId`, `albumId`, `disc`), " +
                    "FOREIGN KEY(`sourceId`, `albumId`) REFERENCES `albums`(`sourceId`, `id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_discs_sourceId_albumId` ON `discs` (`sourceId`, `albumId`)")
        }
    }

val MIGRATION_11_12 =
    object : Migration(11, 12) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `artwork_seeds` ADD COLUMN `nameBusy` INTEGER")
        }
    }

val MIGRATION_12_13 =
    object : Migration(12, 13) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("DROP TABLE IF EXISTS `artwork_seeds`")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `artwork_seeds` (`cacheKey` TEXT NOT NULL, " +
                    "`primary` INTEGER NOT NULL, `secondary` INTEGER, `nameBusy` INTEGER, " +
                    "`sourceId` INTEGER NOT NULL, `storedAt` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`cacheKey`), " +
                    "FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_artwork_seeds_sourceId` ON `artwork_seeds` (`sourceId`)")
        }
    }

val MIGRATION_13_14 =
    object : Migration(13, 14) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `albums` DROP COLUMN `frequentRank`")
            connection.execSQL("ALTER TABLE `albums` DROP COLUMN `recentRank`")
        }
    }

val MIGRATION_14_15 =
    object : Migration(14, 15) {
        override suspend fun migrate(connection: SQLiteConnection) {
            rebuildTable(connection, "albums", ALBUMS_V14, ALBUMS_V14_COPY, ALBUMS_V14_INDICES)
            rebuildTable(connection, "artists", ARTISTS_V14, ARTISTS_V14_COPY, ARTISTS_V14_INDICES)
            rebuildTable(connection, "playlists", PLAYLISTS_V14, PLAYLISTS_V14_COPY, PLAYLISTS_V14_INDICES)
        }
    }

val MIGRATION_15_16 =
    object : Migration(15, 16) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `up_next_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`position` INTEGER NOT NULL, `sourceId` INTEGER NOT NULL, `kind` TEXT NOT NULL, " +
                    "`refId` TEXT NOT NULL, `rangeStart` INTEGER, `rangeEnd` INTEGER, " +
                    "FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_up_next_entries_sourceId` ON `up_next_entries` (`sourceId`)",
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_up_next_entries_position` ON `up_next_entries` (`position`)",
            )
            connection.execSQL("ALTER TABLE `playback_cursor` ADD COLUMN `upNextAnchor` INTEGER NOT NULL DEFAULT 0")
        }
    }

private suspend fun rebuildTable(
    connection: SQLiteConnection,
    table: String,
    createSql: String,
    copySql: String,
    indices: List<String>,
) {
    val temp = "${table}_new"
    connection.execSQL(createSql.replace("\${TABLE_NAME}", temp))
    connection.execSQL(copySql.replace("\${TABLE_NAME}", temp))
    connection.execSQL("DROP TABLE `$table`")
    connection.execSQL("ALTER TABLE `$temp` RENAME TO `$table`")
    indices.forEach { connection.execSQL(it.replace("\${TABLE_NAME}", table)) }
}

private const val ALBUMS_V14 =
    "CREATE TABLE IF NOT EXISTS `\${TABLE_NAME}` (`sourceId` INTEGER NOT NULL, `id` TEXT NOT NULL, `artistId` TEXT, " +
        "`name` TEXT NOT NULL COLLATE NOCASE, `albumArtist` TEXT COLLATE NOCASE, `created` INTEGER NOT NULL, " +
        "`coverArt` TEXT, `genre` TEXT, `year` INTEGER, `starred` INTEGER, `songCount` INTEGER NOT NULL, " +
        "PRIMARY KEY(`sourceId`, `id`), " +
        "FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

private const val ALBUMS_V14_COPY =
    "INSERT INTO `\${TABLE_NAME}` (sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, starred, songCount) " +
        "SELECT sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, starred, songCount FROM `albums`"

private val ALBUMS_V14_INDICES =
    listOf(
        "CREATE INDEX IF NOT EXISTS `index_albums_sourceId` ON `\${TABLE_NAME}` (`sourceId`)",
        "CREATE INDEX IF NOT EXISTS `index_albums_sourceId_artistId` ON `\${TABLE_NAME}` (`sourceId`, `artistId`)",
        "CREATE INDEX IF NOT EXISTS `index_albums_name` ON `\${TABLE_NAME}` (`sourceId`, `name`, `id`)",
        "CREATE INDEX IF NOT EXISTS `index_albums_artist` ON `\${TABLE_NAME}` (`sourceId`, `albumArtist`, `year`, `name`, `id`)",
        "CREATE INDEX IF NOT EXISTS `index_albums_year` ON `\${TABLE_NAME}` (`sourceId` ASC, `year` DESC, `name` ASC, `id` ASC)",
        "CREATE INDEX IF NOT EXISTS `index_albums_added` ON `\${TABLE_NAME}` (`sourceId` ASC, `created` DESC, `name` ASC, `id` ASC)",
        "CREATE INDEX IF NOT EXISTS `index_albums_starred` ON `\${TABLE_NAME}` (`sourceId` ASC, `starred` DESC, `name` ASC, `id` ASC)",
    )

private const val ARTISTS_V14 =
    "CREATE TABLE IF NOT EXISTS `\${TABLE_NAME}` (`sourceId` INTEGER NOT NULL, `id` TEXT NOT NULL, " +
        "`name` TEXT NOT NULL COLLATE NOCASE, `albumCount` INTEGER NOT NULL, `starred` INTEGER, `coverArt` TEXT, " +
        "PRIMARY KEY(`sourceId`, `id`), FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

private const val ARTISTS_V14_COPY =
    "INSERT INTO `\${TABLE_NAME}` (sourceId, id, name, albumCount, starred, coverArt) " +
        "SELECT sourceId, id, name, albumCount, starred, coverArt FROM `artists`"

private val ARTISTS_V14_INDICES =
    listOf(
        "CREATE INDEX IF NOT EXISTS `index_artists_sourceId` ON `\${TABLE_NAME}` (`sourceId`)",
        "CREATE INDEX IF NOT EXISTS `index_artists_name` ON `\${TABLE_NAME}` (`sourceId`, `name`, `id`)",
        "CREATE INDEX IF NOT EXISTS `index_artists_albumCount` ON `\${TABLE_NAME}` (`sourceId` ASC, `albumCount` DESC, `name` ASC, `id` ASC)",
        "CREATE INDEX IF NOT EXISTS `index_artists_starred` ON `\${TABLE_NAME}` (`sourceId` ASC, `starred` DESC, `name` ASC, `id` ASC)",
    )

private const val PLAYLISTS_V14 =
    "CREATE TABLE IF NOT EXISTS `\${TABLE_NAME}` (`sourceId` INTEGER NOT NULL, `id` TEXT NOT NULL, " +
        "`name` TEXT NOT NULL COLLATE NOCASE, `comment` TEXT, `coverArt` TEXT, `songCount` INTEGER NOT NULL, " +
        "`created` INTEGER NOT NULL, `changed` INTEGER NOT NULL DEFAULT 0, `duration` INTEGER NOT NULL DEFAULT 0, " +
        "PRIMARY KEY(`sourceId`, `id`), FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"

private const val PLAYLISTS_V14_COPY =
    "INSERT INTO `\${TABLE_NAME}` (sourceId, id, name, comment, coverArt, songCount, created, changed, duration) " +
        "SELECT sourceId, id, name, comment, coverArt, songCount, created, changed, duration FROM `playlists`"

private val PLAYLISTS_V14_INDICES =
    listOf(
        "CREATE INDEX IF NOT EXISTS `index_playlists_sourceId` ON `\${TABLE_NAME}` (`sourceId`)",
        "CREATE INDEX IF NOT EXISTS `index_playlists_name` ON `\${TABLE_NAME}` (`sourceId`, `name`, `id`)",
        "CREATE INDEX IF NOT EXISTS `index_playlists_added` ON `\${TABLE_NAME}` (`sourceId` ASC, `created` DESC, `name` ASC, `id` ASC)",
        "CREATE INDEX IF NOT EXISTS `index_playlists_updated` ON `\${TABLE_NAME}` (`sourceId` ASC, `changed` DESC, `name` ASC, `id` ASC)",
    )

val MIGRATION_16_17 =
    object : Migration(16, 17) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `song_downloads` (`sourceId` INTEGER NOT NULL, `songId` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, `engineId` INTEGER, `bytes` INTEGER NOT NULL DEFAULT 0, " +
                    "`total` INTEGER NOT NULL DEFAULT 0, `error` TEXT, `queuedAt` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`sourceId`, `songId`), FOREIGN KEY(`sourceId`) REFERENCES `sources`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_song_downloads_sourceId` ON `song_downloads` (`sourceId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_song_downloads_engineId` ON `song_downloads` (`engineId`)")
        }
    }

val MIGRATIONS: Array<Migration> =
    arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
    )
