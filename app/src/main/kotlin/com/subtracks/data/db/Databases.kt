package com.subtracks.data.db

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun createAndroidDatabase(context: Context): SubtracksDatabase =
    Room
        .databaseBuilder<SubtracksDatabase>(context, "subtracks.db")
        .setDriver(BundledSQLiteDriver())
        .addMigrations(*MIGRATIONS)
        .build()
