package com.subtracks.data

import android.content.Context
import com.subtracks.log.Log
import java.io.File

// One-shot cleanup of the 1.x React Native app, which shipped as com.subtracks before this rewrite.
// It left an AsyncStorage database (RKStorage), an older `servers` dump and a downloaded/cached
// artwork+audio tree (under the app's external files dir) that v3 never reads. Only a real 1.x upgrade
// has those paths, so a fresh install does nothing and an upgrade is cleaned exactly once.
//
// Delete this file and its call in SubtracksApp once installs can no longer be coming from 1.x.
internal object LegacyDataCleanup {
    fun run(context: Context) {
        // The cached trees can be large, so keep the walk off the main thread.
        Thread { run(context.dataDir, context.getExternalFilesDir(null)) }.start()
    }

    internal fun run(
        dataDir: File,
        externalFilesDir: File?,
    ) {
        val database = File(dataDir, "databases/RKStorage")
        val servers = File(dataDir, "files/servers")
        val cache = externalFilesDir?.let { File(it, "s") }
        if (!database.exists() && !servers.exists() && cache?.exists() != true) return
        deleteDatabase(database)
        deleteTree(servers)
        cache?.let(::deleteTree)
    }

    private fun deleteDatabase(database: File) {
        listOf("", "-wal", "-shm", "-journal").forEach { deleteTree(File(database.path + it)) }
    }

    private fun deleteTree(target: File) {
        if (!target.exists()) return
        val removed = target.deleteRecursively()
        Log.i("legacy", "removed ${target.path} ($removed)")
    }
}
