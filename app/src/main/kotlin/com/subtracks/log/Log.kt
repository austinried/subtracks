package com.subtracks.log

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.subtracks.BuildConfig
import com.subtracks.R
import com.subtracks.data.db.SqlExecution
import okhttp3.Interceptor
import java.io.File
import java.io.IOException
import kotlin.math.roundToLong
import android.util.Log as AndroidLog

object Log {
    private const val SLOW_QUERY_MS = 100.0
    private const val MAX_SQL_CHARS = 500
    private val WHITESPACE = Regex("\\s+")

    private var fileStore: LogFileStore? = null
    private var appContext: Context? = null
    private var consoleLevel = LogLevel.DEBUG
    private var crashHandlerInstalled = false

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        consoleLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.INFO
        val fileLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO
        fileStore?.shutdown()
        fileStore = LogFileStore(File(app.filesDir, "logs"), fileLevel)
        installCrashHandler()
        i("app", "started ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) on Android ${Build.VERSION.SDK_INT} ${Build.MODEL}")
    }

    fun v(
        tag: String,
        message: String,
    ) = log(LogLevel.VERBOSE, tag, message, null)

    fun d(
        tag: String,
        message: String,
    ) = log(LogLevel.DEBUG, tag, message, null)

    fun i(
        tag: String,
        message: String,
    ) = log(LogLevel.INFO, tag, message, null)

    fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) = log(LogLevel.WARN, tag, message, throwable)

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) = log(LogLevel.ERROR, tag, message, throwable)

    fun exportZip(): File? {
        val store = fileStore ?: return null
        val context = appContext ?: return null
        store.flush()
        return store.exportZip(File(context.cacheDir, "log_dumps"))
    }

    fun share(
        context: Context,
        file: File,
    ) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send =
            Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.settings_about_share_logs_subject))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(
            Intent.createChooser(send, context.getString(R.string.settings_about_actions_share_logs)),
        )
    }

    fun interceptor(): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            v("http", "${request.method} ${request.url}")
            try {
                val response = chain.proceed(request)
                if (response.code >= 400) {
                    w("http", "${request.method} ${request.url} -> ${response.code}")
                }
                response
            } catch (failure: IOException) {
                if (chain.call().isCanceled()) {
                    v("http", "${request.method} ${request.url} cancelled")
                } else {
                    w("http", "${request.method} ${request.url} failed", failure)
                }
                throw failure
            }
        }

    fun sql(execution: SqlExecution) {
        val failure = execution.error
        if (failure != null) {
            if (isUpsertConflict(execution.sql, failure)) {
                v("sql", "upsert fallback conflict: ${shortSql(execution.sql)}")
            } else {
                e("sql", "failed: ${shortSql(execution.sql)}", failure)
            }
            return
        }
        if (!isSlowQuery(execution, SLOW_QUERY_MS)) return
        val rows = if (isReadStatement(execution.sql)) " rows=${execution.rows}" else ""
        w("sql", "slow ${execution.durationMillis.roundToLong()}ms$rows: ${shortSql(execution.sql)}")
    }

    private fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val redacted = redactLogs(message)
        if (level.ordinal >= consoleLevel.ordinal) {
            // Logging is best effort: a logcat write must never disturb the caller.
            runCatching {
                val text =
                    if (throwable != null) {
                        "$redacted\n${redactLogs(AndroidLog.getStackTraceString(throwable))}"
                    } else {
                        redacted
                    }
                AndroidLog.println(priority(level), tag.take(23), text)
            }
        }
        fileStore?.write(level, tag, redacted, throwable)
    }

    private fun priority(level: LogLevel): Int =
        when (level) {
            LogLevel.VERBOSE -> AndroidLog.VERBOSE
            LogLevel.DEBUG -> AndroidLog.DEBUG
            LogLevel.INFO -> AndroidLog.INFO
            LogLevel.WARN -> AndroidLog.WARN
            LogLevel.ERROR -> AndroidLog.ERROR
        }

    private fun installCrashHandler() {
        if (crashHandlerInstalled) return
        crashHandlerInstalled = true
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                fileStore?.writeImmediately(LogLevel.ERROR, "crash", "uncaught exception on ${thread.name}", throwable)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun shortSql(sql: String): String = sql.replace(WHITESPACE, " ").take(MAX_SQL_CHARS)
}

private val DATA_KEYWORDS = setOf("SELECT", "INSERT", "UPDATE", "DELETE", "WITH", "REPLACE")
private val READ_KEYWORDS = setOf("SELECT", "WITH")
private const val SQL_INTERNAL = "room_table_modification_log"

internal fun isDataStatement(sql: String): Boolean {
    val normalized = sql.trimStart()
    if (normalized.contains(SQL_INTERNAL)) return false
    return normalized.takeWhile { !it.isWhitespace() }.uppercase() in DATA_KEYWORDS
}

internal fun isReadStatement(sql: String): Boolean = sql.trimStart().takeWhile { !it.isWhitespace() }.uppercase() in READ_KEYWORDS

internal fun isSlowQuery(
    execution: SqlExecution,
    thresholdMs: Double,
): Boolean = execution.error == null && execution.durationMillis >= thresholdMs && isDataStatement(execution.sql)

// Room's EntityUpsertAdapter implements @Upsert as an INSERT and treats a uniqueness conflict as the
// signal to UPDATE instead (EntityUpsertAdapter.checkUniquenessException). Only downgrade a failed
// INSERT whose error says the conflict was a unique/primary key; anything else stays an ERROR so a
// genuine failure isn't hidden in release.
private const val CONSTRAINT_UNIQUE_MESSAGE = "unique constraint failed"
private const val CONSTRAINT_PRIMARY_KEY_MESSAGE = "primary key constraint failed"

internal fun isUpsertConflict(
    sql: String,
    error: Throwable,
): Boolean {
    if (!sql.trimStart().uppercase().startsWith("INSERT")) return false
    val message = error.message?.lowercase() ?: return false
    return message.contains(CONSTRAINT_UNIQUE_MESSAGE) || message.contains(CONSTRAINT_PRIMARY_KEY_MESSAGE)
}
