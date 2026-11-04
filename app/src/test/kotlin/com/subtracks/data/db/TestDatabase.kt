package com.subtracks.data.db

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider

fun createTestDatabase(driver: SQLiteDriver = BundledSQLiteDriver()): SubtracksDatabase {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return Room
        .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
        .setDriver(driver)
        .addSubtracksDatabaseCallbacks()
        .build()
}
