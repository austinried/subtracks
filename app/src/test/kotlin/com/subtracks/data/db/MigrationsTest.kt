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
