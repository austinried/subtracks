package com.subtracks.data.db

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbLoggingTest {
    private val executions = mutableListOf<SqlExecution>()

    private fun driver() = LoggingSQLiteDriver(BundledSQLiteDriver()) { executions += it }

    @Test
    fun forwardsConnectionPoolFlag() {
        assertTrue(LoggingSQLiteDriver(FakeDriver(hasConnectionPool = true)) {}.hasConnectionPool)
        assertFalse(LoggingSQLiteDriver(FakeDriver(hasConnectionPool = false)) {}.hasConnectionPool)
    }

    @Test
    fun recordsOneExecutionPerBatchedWriteRow() {
        driver().open(":memory:").use { connection ->
            connection.prepare("CREATE TABLE t (id INTEGER PRIMARY KEY, name TEXT)").use { it.step() }
            connection.prepare("INSERT INTO t (name) VALUES (?)").use { statement ->
                repeat(3) { index ->
                    statement.bindText(1, "row$index")
                    statement.step()
                    statement.reset()
                    statement.clearBindings()
                }
            }
        }
        assertEquals(3, executions.count { it.sql.startsWith("INSERT") })
    }

    @Test
    fun recordsSuccessfulSelect() {
        driver().open(":memory:").use { connection ->
            connection.prepare("CREATE TABLE t (id INTEGER PRIMARY KEY, name TEXT)").use { it.step() }
            connection.prepare("INSERT INTO t (name) VALUES (?)").use {
                it.bindText(1, "a")
                it.step()
            }
            connection.prepare("SELECT * FROM t").use { statement ->
                while (statement.step()) {
                }
            }
        }
        val select = executions.last { it.sql.startsWith("SELECT") }
        assertNull(select.error)
        assertEquals(1L, select.rows)
        assertTrue(select.durationNanos > 0)
    }

    @Test
    fun recordsPrepareError() {
        driver().open(":memory:").use { connection ->
            runCatching { connection.prepare("THIS IS NOT SQL") }
        }
        val failed = executions.single()
        assertNotNull(failed.error)
        assertEquals("THIS IS NOT SQL", failed.sql)
    }

    @Test
    fun recordsStepError() {
        driver().open(":memory:").use { connection ->
            connection.prepare("CREATE TABLE t (id INTEGER PRIMARY KEY)").use { it.step() }
            connection.prepare("INSERT INTO t (id) VALUES (1)").use { it.step() }
            runCatching {
                connection.prepare("INSERT INTO t (id) VALUES (1)").use { it.step() }
            }
        }
        val failed = executions.last { it.error != null }
        assertTrue(failed.sql.startsWith("INSERT"))
    }

    @Test
    fun loggedMigrationRunsDelegate() =
        runTest {
            val driver = LoggingSQLiteDriver(BundledSQLiteDriver()) {}
            val migration =
                LoggedMigration(
                    object : Migration(1, 2) {
                        override suspend fun migrate(connection: SQLiteConnection) {
                            connection.execSQL("CREATE TABLE m (id INTEGER)")
                        }
                    },
                )
            driver.open(":memory:").use { connection ->
                migration.migrate(connection)
                connection.prepare("SELECT count(*) FROM m").use { statement ->
                    statement.step()
                    assertEquals(0L, statement.getLong(0))
                }
            }
        }

    @Test
    fun loggedMigrationRethrows() =
        runTest {
            val driver = LoggingSQLiteDriver(BundledSQLiteDriver()) {}
            val migration =
                LoggedMigration(
                    object : Migration(1, 2) {
                        override suspend fun migrate(connection: SQLiteConnection) {
                            error("nope")
                        }
                    },
                )
            driver.open(":memory:").use { connection ->
                assertTrue(runCatching { migration.migrate(connection) }.isFailure)
            }
        }
}

private class FakeDriver(
    override val hasConnectionPool: Boolean,
) : SQLiteDriver {
    override fun open(fileName: String): SQLiteConnection = error("open should not be called")
}
