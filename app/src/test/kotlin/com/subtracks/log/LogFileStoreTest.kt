package com.subtracks.log

import com.subtracks.data.db.SqlExecution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class LogFileStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val root: File by lazy { folder.newFolder("logs") }

    @Test
    fun writesFormattedLine() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(LogLevel.INFO, "sync", "hello world", null)
        store.flush()
        val text = store.files().single().readText()
        assertTrue(text, Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} I sync hello world""").containsMatchIn(text))
    }

    @Test
    fun redactsSubsonicParams() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(LogLevel.INFO, "http", "http://host/rest/stream?u=alice&p=hunter2&s=salt&t=tok&id=7", null)
        store.flush()
        val text = store.files().single().readText()
        assertTrue(text, text.contains("u=REDACTED"))
        assertTrue(text, text.contains("p=REDACTED"))
        assertTrue(text, text.contains("&id=7"))
        assertFalse(text, text.contains("hunter2"))
        assertFalse(text, text.contains("salt"))
        assertFalse(text, text.contains("tok"))
    }

    @Test
    fun redactsBasicAuth() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(LogLevel.INFO, "http", "http://alice:hunter2@music.example.org/rest/ping", null)
        store.flush()
        val text = store.files().single().readText()
        assertTrue(text, text.contains("http://REDACTED@music.example.org/rest/ping"))
        assertFalse(text, text.contains("hunter2"))
    }

    @Test
    fun includesStackTrace() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(LogLevel.ERROR, "sql", "boom", IllegalStateException("kaboom"))
        store.flush()
        val text = store.files().single().readText()
        assertTrue(text, text.contains("IllegalStateException: kaboom"))
    }

    @Test
    fun redactsCredentialsInThrowable() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(
            LogLevel.ERROR,
            "http",
            "failed",
            IllegalStateException("GET http://alice:secret@host/x?u=bob&p=hunter2 failed"),
        )
        store.flush()
        val text = store.files().single().readText()
        assertTrue(text, text.contains("REDACTED@host"))
        assertFalse(text, text.contains("secret"))
        assertFalse(text, text.contains("hunter2"))
    }

    @Test
    fun dropsBelowMinimumLevel() {
        val store = LogFileStore(root, LogLevel.WARN)
        store.write(LogLevel.INFO, "x", "quiet", null)
        store.write(LogLevel.WARN, "x", "loud", null)
        store.flush()
        val text = store.files().single().readText()
        assertFalse(text, text.contains("quiet"))
        assertTrue(text, text.contains("loud"))
    }

    @Test
    fun rotatesWhenFileExceedsCap() {
        val store = LogFileStore(root, LogLevel.VERBOSE, maxFileBytes = 1)
        repeat(4) { store.write(LogLevel.INFO, "x", "line $it", null) }
        store.flush()
        assertEquals(4, store.files().size)
    }

    @Test
    fun prunesToMaxFiles() {
        val store = LogFileStore(root, LogLevel.VERBOSE, maxFileBytes = 1, maxFiles = 3)
        repeat(10) { store.write(LogLevel.INFO, "x", "line $it", null) }
        store.flush()
        assertEquals(3, store.files().size)
    }

    @Test
    fun exportZipContainsLogs() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        store.write(LogLevel.INFO, "x", "one", null)
        store.flush()
        val zip = store.exportZip(folder.newFolder("out"))
        assertTrue(zip != null)
        ZipFile(zip!!).use { archive ->
            assertEquals(1, archive.size())
            assertTrue(
                archive
                    .entries()
                    .nextElement()
                    .name
                    .endsWith(".log"),
            )
        }
    }

    @Test
    fun exportZipIsNullWhenEmpty() {
        val store = LogFileStore(root, LogLevel.VERBOSE)
        assertNull(store.exportZip(folder.newFolder("out")))
    }

    @Test
    fun slowQueryPredicateExcludesNonQueriesAndInternalSql() {
        assertTrue(isSlowQuery(execution(150.0, "SELECT * FROM songs"), 100.0))
        assertFalse(isSlowQuery(execution(50.0, "SELECT * FROM songs"), 100.0))
        assertFalse(isSlowQuery(execution(150.0, "BEGIN IMMEDIATE TRANSACTION"), 100.0))
        assertFalse(isSlowQuery(execution(150.0, "SELECT * FROM room_table_modification_log WHERE invalidated = 1"), 100.0))
    }

    @Test
    fun classifiesDataStatements() {
        assertTrue(isDataStatement("insert into t values (1)"))
        assertTrue(isDataStatement("  SELECT 1"))
        assertFalse(isDataStatement("PRAGMA user_version"))
        assertFalse(isDataStatement("END TRANSACTION"))
    }

    @Test
    fun classifiesReadStatements() {
        assertTrue(isReadStatement("select * from songs"))
        assertTrue(isReadStatement("WITH x AS (SELECT 1) SELECT * FROM x"))
        assertFalse(isReadStatement("INSERT INTO t VALUES (1)"))
    }

    private fun execution(
        milliseconds: Double,
        sql: String,
    ) = SqlExecution(sql, (milliseconds * 1_000_000).toLong(), 0, null)
}
