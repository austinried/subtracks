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

val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
