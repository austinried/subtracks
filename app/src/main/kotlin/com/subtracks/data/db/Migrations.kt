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
    )
