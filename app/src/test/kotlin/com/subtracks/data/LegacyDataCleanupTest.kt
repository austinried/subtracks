package com.subtracks.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LegacyDataCleanupTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun removesThe1xLeftoversAndLeavesTheNativeData() {
        val dataDir = tmp.newFolder("data")
        val external = tmp.newFolder("external")

        val legacyDatabase = file(dataDir, "databases/RKStorage")
        val legacyDatabaseWal = file(dataDir, "databases/RKStorage-wal")
        val legacyServers = file(dataDir, "files/servers/0.json")
        val legacyCache = file(external, "s/0/coverArt/cover")
        val nativeDatabase = file(dataDir, "databases/subtracks.db")
        val nativeDownload = file(external, "Music/downloads/song.mp3")

        LegacyDataCleanup.run(dataDir, external)

        assertFalse(legacyDatabase.exists())
        assertFalse(legacyDatabaseWal.exists())
        assertFalse(legacyServers.exists())
        assertFalse(legacyCache.exists())
        assertTrue(nativeDatabase.exists())
        assertTrue(nativeDownload.exists())
    }

    @Test
    fun leavesTheNativeDataAloneOnAFreshInstall() {
        val dataDir = tmp.newFolder("data2")
        val external = tmp.newFolder("external2")
        val nativeDatabase = file(dataDir, "databases/subtracks.db")
        val nativeDownload = file(external, "Music/downloads/song.mp3")

        LegacyDataCleanup.run(dataDir, external)

        assertTrue(nativeDatabase.exists())
        assertTrue(nativeDownload.exists())
    }

    @Test
    fun cleansUpEvenWhenExternalStorageIsUnavailable() {
        val dataDir = tmp.newFolder("data3")
        val legacyDatabase = file(dataDir, "databases/RKStorage")

        LegacyDataCleanup.run(dataDir, null)

        assertFalse(legacyDatabase.exists())
    }

    private fun file(
        dir: File,
        path: String,
    ): File =
        File(dir, path).apply {
            parentFile?.mkdirs()
            createNewFile()
        }
}
