package com.subtracks.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.subtracks.log.Log

fun createAndroidDatabase(context: Context): SubtracksDatabase {
    val driver = LoggingSQLiteDriver(BundledSQLiteDriver(), Log::sql)
    return Room
        .databaseBuilder<SubtracksDatabase>(context, "subtracks.db")
        .setDriver(driver)
        .addMigrations(*MIGRATIONS.map(::LoggedMigration).toTypedArray())
        .addSubtracksDatabaseCallbacks()
        .build()
}

internal fun RoomDatabase.Builder<SubtracksDatabase>.addSubtracksDatabaseCallbacks(): RoomDatabase.Builder<SubtracksDatabase> =
    addCallback(ArtistSearchTriggerOptimization).addCallback(SqliteLifecycleLogging)
