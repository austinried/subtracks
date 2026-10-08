package com.subtracks.data.db

import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import com.subtracks.log.Log
import kotlinx.coroutines.CancellationException

data class SqlExecution(
    val sql: String,
    val durationNanos: Long,
    val rows: Long,
    val error: Throwable?,
) {
    val durationMillis: Double get() = durationNanos / 1_000_000.0
}

class LoggingSQLiteDriver(
    private val delegate: SQLiteDriver,
    private val onExecution: (SqlExecution) -> Unit,
) : SQLiteDriver {
    override val hasConnectionPool: Boolean
        get() = delegate.hasConnectionPool

    override fun open(fileName: String): SQLiteConnection = LoggingConnection(delegate.open(fileName), onExecution)
}

private class LoggingConnection(
    private val delegate: SQLiteConnection,
    private val onExecution: (SqlExecution) -> Unit,
) : SQLiteConnection by delegate {
    override fun prepare(sql: String): SQLiteStatement {
        val start = System.nanoTime()
        return try {
            LoggingStatement(delegate.prepare(sql), sql, onExecution)
        } catch (failure: Throwable) {
            onExecution(SqlExecution(sql, System.nanoTime() - start, 0, failure))
            throw failure
        }
    }
}

private class LoggingStatement(
    private val delegate: SQLiteStatement,
    private val sql: String,
    private val onExecution: (SqlExecution) -> Unit,
) : SQLiteStatement by delegate {
    private var startNanos = 0L
    private var rows = 0L
    private var failure: Throwable? = null
    private var active = false

    override fun step(): Boolean {
        begin()
        val result =
            try {
                delegate.step()
            } catch (error: Throwable) {
                failure = error
                report()
                throw error
            }
        if (result) rows++
        if (!result) report()
        return result
    }

    override fun reset() {
        report()
        delegate.reset()
    }

    override fun close() {
        report()
        delegate.close()
    }

    private fun report() {
        if (!active) return
        active = false
        onExecution(SqlExecution(sql, System.nanoTime() - startNanos, rows, failure))
    }

    // Started on the first step, not on reset: Room's statement cache resets a statement when it is
    // returned to the cache (BasePreparedStatementCache.CachedStatement.close), so timing from reset
    // would count however long the statement idled there before its next use.
    private fun begin() {
        if (active) return
        startNanos = System.nanoTime()
        rows = 0
        failure = null
        active = true
    }
}

class LoggedMigration(
    private val delegate: Migration,
) : Migration(delegate.startVersion, delegate.endVersion) {
    override suspend fun migrate(connection: SQLiteConnection) {
        val label = "${delegate.startVersion} -> ${delegate.endVersion}"
        Log.i("sql", "migrating $label")
        val start = System.nanoTime()
        try {
            delegate.migrate(connection)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            Log.e("sql", "migration $label failed after ${(System.nanoTime() - start) / 1_000_000}ms", failure)
            throw failure
        }
        Log.i("sql", "migrated $label in ${(System.nanoTime() - start) / 1_000_000}ms")
    }
}

internal object SqliteLifecycleLogging : RoomDatabase.Callback() {
    override suspend fun onCreate(connection: SQLiteConnection) {
        Log.i("sql", "created fresh database")
    }

    override suspend fun onOpen(connection: SQLiteConnection) {
        Log.i("sql", "database opened")
    }

    override suspend fun onDestructiveMigration(connection: SQLiteConnection) {
        Log.w("sql", "destructive migration: dropping all tables")
    }
}
