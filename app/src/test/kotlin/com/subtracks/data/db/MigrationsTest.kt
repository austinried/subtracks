package com.subtracks.data.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MigrationsTest {
    private val driver = BundledSQLiteDriver()
    private val schemaDirectory =
        listOf(File("schemas"), File("app/schemas"))
            .map { File(it, SCHEMA_PATH) }
            .firstOrNull { it.isDirectory }
            ?: error("Schema directory not found under schemas/ or app/schemas/")

    private val versions =
        schemaDirectory
            .listFiles { file -> file.name.endsWith(".json") }!!
            .map { it.nameWithoutExtension.toInt() }
            .sorted()

    @Test
    fun everyExportedVersionIsCoveredByAContiguousMigrationChain() {
        val chain = MIGRATIONS.sortedBy { it.startVersion }
        assertEquals(versions.dropLast(1), chain.map { it.startVersion })
        assertEquals(versions.drop(1), chain.map { it.endVersion })
    }

    @Test
    fun theMigrationChainProducesTheCurrentSchema() =
        runTest {
            driver.open(":memory:").use { migrated ->
                applySchema(migrated, versions.first())
                MIGRATIONS.sortedBy { it.startVersion }.forEach { it.migrate(migrated) }

                driver.open(":memory:").use { expected ->
                    applySchema(expected, versions.last())
                    assertEquals(schemaShape(expected), schemaShape(migrated))
                }
            }
        }

    @Test
    fun theLibrarySortIndicesInheritCaseInsensitiveCollationAndDirection() =
        runTest {
            driver.open(":memory:").use { migrated ->
                applySchema(migrated, versions.first())
                MIGRATIONS.sortedBy { it.startVersion }.forEach { it.migrate(migrated) }

                assertEquals("NOCASE", indexColumn(migrated, "index_albums_name", "name").collation)
                assertEquals("NOCASE", indexColumn(migrated, "index_albums_artist", "albumArtist").collation)
                assertEquals("NOCASE", indexColumn(migrated, "index_albums_artist", "name").collation)
                assertEquals(1, indexColumn(migrated, "index_albums_year", "year").descending)
                assertEquals("NOCASE", indexColumn(migrated, "index_albums_year", "name").collation)
                assertEquals(0, indexColumn(migrated, "index_albums_year", "name").descending)
                assertEquals(1, indexColumn(migrated, "index_albums_added", "created").descending)
                assertEquals(1, indexColumn(migrated, "index_albums_starred", "starred").descending)
                assertEquals(1, indexColumn(migrated, "index_albums_frequent", "playCount").descending)
                assertEquals(1, indexColumn(migrated, "index_albums_recent", "played").descending)
                assertEquals("NOCASE", indexColumn(migrated, "index_artists_name", "name").collation)
                assertEquals(1, indexColumn(migrated, "index_artists_albumCount", "albumCount").descending)
                assertEquals(1, indexColumn(migrated, "index_artists_starred", "starred").descending)
                assertEquals(1, indexColumn(migrated, "index_artists_frequent", "playCount").descending)
                assertEquals(1, indexColumn(migrated, "index_artists_recent", "played").descending)
                assertEquals("NOCASE", indexColumn(migrated, "index_playlists_name", "name").collation)
                assertEquals(1, indexColumn(migrated, "index_playlists_added", "created").descending)
                assertEquals(1, indexColumn(migrated, "index_playlists_updated", "changed").descending)
            }
        }

    @Test
    fun thePlaylistChangedAndDurationColumnsSurviveTheRebuild() =
        runTest {
            driver.open(":memory:").use { migrated ->
                applySchema(migrated, versions.first())
                migrated.execSQL(
                    "INSERT INTO sources (id, name, address, isActive, createdAt) " +
                        "VALUES (1, 'server', 'http://localhost', 1, 100)",
                )
                migrated.execSQL(
                    "INSERT INTO artists (sourceId, id, name, albumCount, starred) " +
                        "VALUES (1, 'ar1', 'The Artist', 1, NULL)",
                )
                migrated.execSQL(
                    "INSERT INTO albums (sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, " +
                        "starred, songCount, frequentRank, recentRank) " +
                        "VALUES (1, 'al1', 'ar1', 'The Album', 'The Artist', 101, NULL, NULL, 1999, 555, 1, NULL, NULL)",
                )
                migrated.execSQL(
                    "INSERT INTO playlists (sourceId, id, name, comment, coverArt, songCount, created) " +
                        "VALUES (1, 'pl1', 'The Playlist', NULL, NULL, 3, 102)",
                )

                MIGRATIONS.sortedBy { it.startVersion }.forEach { migration ->
                    migration.migrate(migrated)
                    when (migration.endVersion) {
                        5 -> {
                            migrated.execSQL("UPDATE playlists SET duration = 987654 WHERE id = 'pl1'")
                        }

                        8 -> {
                            migrated.execSQL("UPDATE playlists SET changed = 123456 WHERE id = 'pl1'")
                        }

                        11 -> {
                            migrated.execSQL("INSERT INTO discs (sourceId, albumId, disc, title) VALUES (1, 'al1', 2, 'The Disc')")
                        }

                        18 -> {
                            migrated.execSQL(
                                "INSERT INTO song_downloads (sourceId, songId, status, bytes, total) " +
                                    "VALUES (1, 's1', 'Completed', 10, 20)",
                            )
                        }
                    }
                }

                assertEquals("The Playlist", text(migrated, "SELECT name FROM playlists WHERE id = 'pl1'"))
                assertEquals(123456L, long(migrated, "SELECT changed FROM playlists WHERE id = 'pl1'"))
                assertEquals(987654L, long(migrated, "SELECT duration FROM playlists WHERE id = 'pl1'"))
                assertEquals("The Album", text(migrated, "SELECT name FROM albums WHERE id = 'al1'"))
                assertEquals(555L, long(migrated, "SELECT starred FROM albums WHERE id = 'al1'"))
                assertEquals("The Artist", text(migrated, "SELECT name FROM artists WHERE id = 'ar1'"))
                assertEquals("The Disc", text(migrated, "SELECT title FROM discs WHERE albumId = 'al1' AND disc = 2"))
                assertEquals("Completed", text(migrated, "SELECT status FROM song_downloads WHERE songId = 's1'"))
                assertEquals(10L, long(migrated, "SELECT bytes FROM song_downloads WHERE songId = 's1'"))
            }
        }

    @Test
    fun theGenreBackfillTrimsAndSkipsBlankSingularGenres() =
        runTest {
            driver.open(":memory:").use { migrated ->
                applySchema(migrated, versions.first())
                MIGRATIONS.filter { it.endVersion <= 23 }.sortedBy { it.startVersion }.forEach { it.migrate(migrated) }
                migrated.execSQL(
                    "INSERT INTO sources (id, name, address, isActive, createdAt) VALUES (1, 's', 'http://x', 1, 0)",
                )
                migrated.execSQL("INSERT INTO songs (sourceId, id, title, genre) VALUES (1, 's1', 'Song', '  Rock ')")
                migrated.execSQL("INSERT INTO songs (sourceId, id, title, genre) VALUES (1, 's2', 'Blank', '   ')")

                MIGRATIONS.first { it.endVersion == 24 }.migrate(migrated)

                assertEquals("Rock", text(migrated, "SELECT genre FROM song_genres WHERE songId = 's1'"))
                assertEquals(0L, long(migrated, "SELECT COUNT(*) FROM song_genres WHERE songId = 's2'"))
            }
        }

    private fun text(
        connection: SQLiteConnection,
        sql: String,
    ): String =
        connection.prepare(sql).use { statement ->
            check(statement.step()) { "No row for $sql" }
            statement.getText(0)
        }

    private fun long(
        connection: SQLiteConnection,
        sql: String,
    ): Long =
        connection.prepare(sql).use { statement ->
            check(statement.step()) { "No row for $sql" }
            statement.getLong(0)
        }

    private fun indexColumn(
        connection: SQLiteConnection,
        index: String,
        column: String,
    ): IndexColumn {
        connection.prepare("PRAGMA index_xinfo(`$index`)").use { statement ->
            while (statement.step()) {
                if (!statement.isNull(2) && statement.getText(2) == column) {
                    return IndexColumn(collation = statement.getText(4), descending = statement.getInt(3))
                }
            }
        }
        error("Column $column not found in index $index")
    }

    private data class IndexColumn(
        val collation: String,
        val descending: Int,
    )

    private fun applySchema(
        connection: SQLiteConnection,
        version: Int,
    ) {
        val entities = readSchema(version).getJSONObject("database").getJSONArray("entities")
        for (index in 0 until entities.length()) {
            val entity = entities.getJSONObject(index)
            val name = entity.getString("tableName")
            connection.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", name))
            val indices = entity.optJSONArray("indices") ?: continue
            for (i in 0 until indices.length()) {
                connection.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", name))
            }
        }
    }

    private fun readSchema(version: Int): JSONObject = JSONObject(File(schemaDirectory, "$version.json").readText())

    private fun schemaShape(connection: SQLiteConnection): Map<String, TableShape> {
        val tableNames = mutableListOf<String>()
        connection.prepare("SELECT name FROM sqlite_master WHERE type = 'table'").use { statement ->
            while (statement.step()) tableNames += statement.getText(0)
        }
        return tableNames
            .filterNot { it.startsWith("sqlite_") }
            .associateWith { table -> tableShape(connection, table) }
    }

    private fun tableShape(
        connection: SQLiteConnection,
        table: String,
    ): TableShape {
        val columns = linkedMapOf<String, String>()
        connection.prepare("PRAGMA table_info(`$table`)").use { statement ->
            while (statement.step()) {
                val default = if (statement.isNull(4)) "" else statement.getText(4)
                columns[statement.getText(1)] =
                    "${statement.getText(2)}/${statement.getInt(3)}/${statement.getInt(5)}/$default"
            }
        }
        val foreignKeys = linkedMapOf<String, String>()
        connection.prepare("PRAGMA foreign_key_list(`$table`)").use { statement ->
            while (statement.step()) {
                foreignKeys[statement.getText(3)] =
                    "${statement.getText(2)}.${statement.getText(4)}/${statement.getText(5)}/${statement.getText(6)}"
            }
        }
        val indices = linkedMapOf<String, String>()
        connection.prepare("PRAGMA index_list(`$table`)").use { statement ->
            while (statement.step()) {
                val name = statement.getText(1)
                val columns = mutableListOf<String>()
                connection.prepare("PRAGMA index_info(`$name`)").use { info ->
                    while (info.step()) columns += info.getText(2)
                }
                indices[name] = "${statement.getInt(2)}:${columns.joinToString(",")}"
            }
        }
        return TableShape(columns, foreignKeys, indices)
    }

    private data class TableShape(
        val columns: Map<String, String>,
        val foreignKeys: Map<String, String>,
        val indices: Map<String, String>,
    )

    private companion object {
        const val SCHEMA_PATH = "com.subtracks.data.db.SubtracksDatabase"
    }
}
