package com.subtracks.log

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

class LogFileStore(
    private val rootDir: File,
    private val minLevel: LogLevel,
    private val maxFileBytes: Long = DEFAULT_MAX_FILE_BYTES,
    private val maxFiles: Int = DEFAULT_MAX_FILES,
) {
    private val executor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "log-writer").apply { isDaemon = true }
        }
    private val lock = Any()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val lineFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private var current = currentFile()

    init {
        prune()
    }

    fun write(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (level.ordinal < minLevel.ordinal) return
        runCatching {
            executor.execute { runCatching { synchronized(lock) { append(level, tag, message, throwable) } } }
        }
    }

    fun writeImmediately(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (level.ordinal < minLevel.ordinal) return
        runCatching { synchronized(lock) { append(level, tag, message, throwable) } }
    }

    fun flush() {
        runCatching { executor.submit {}.get() }
    }

    fun shutdown() {
        executor.shutdownNow()
    }

    fun files(): List<File> =
        rootDir
            .listFiles { file -> file.isFile && file.name.endsWith(LOG_SUFFIX) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

    fun exportZip(destDir: File): File? =
        synchronized(lock) {
            val logs = files()
            if (logs.isEmpty()) return null
            destDir.mkdirs()
            destDir.listFiles()?.forEach { if (it.isFile) it.delete() }
            val zip = File(destDir, "subtracks-logs-${System.currentTimeMillis()}.zip")
            ZipOutputStream(zip.outputStream().buffered()).use { out ->
                logs.forEach { log ->
                    if (!log.isFile) return@forEach
                    out.putNextEntry(ZipEntry(log.name))
                    log.inputStream().use { it.copyTo(out) }
                    out.closeEntry()
                }
            }
            zip
        }

    private fun append(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val text =
            buildString {
                append(lineFormat.format(Date()))
                append(' ')
                append(LEVEL_CHARS[level.ordinal])
                append(' ')
                append(tag)
                append(' ')
                append(message)
                throwable?.let {
                    append('\n')
                    append(it.stackTraceToString().trimEnd())
                }
                append('\n')
            }
        rootDir.mkdirs()
        var rolled = false
        val today = currentFile()
        if (current.name != today.name) {
            current = today
            rolled = true
        }
        if (current.length() >= maxFileBytes) {
            roll()
            rolled = true
        }
        current.appendText(redactLogs(text))
        if (rolled) prune()
    }

    private var rollSequence = 0

    private fun roll() {
        if (current.exists()) {
            current.renameTo(uniqueRolledFile())
        }
        current = currentFile()
    }

    private fun uniqueRolledFile(): File {
        val day = dateFormat.format(Date())
        while (true) {
            val candidate = File(rootDir, "$day-${rollSequence++}$LOG_SUFFIX")
            if (!candidate.exists()) return candidate
        }
    }

    private fun prune() {
        val logs = files()
        if (logs.size > maxFiles) logs.drop(maxFiles).forEach { it.delete() }
    }

    private fun currentFile(): File = File(rootDir, dateFormat.format(Date()) + LOG_SUFFIX)

    companion object {
        const val DEFAULT_MAX_FILE_BYTES = 1L * 1024 * 1024
        const val DEFAULT_MAX_FILES = 7
        private const val LOG_SUFFIX = ".log"
        private val LEVEL_CHARS = charArrayOf('V', 'D', 'I', 'W', 'E')
    }
}

private val URL_USERINFO = Regex("([a-zA-Z][a-zA-Z0-9+.\\-]*://)[^/@\\s]+@")
private val URL_PARAM = Regex("([?&](?:u|p|s|t)=)[^&\\s]+")

internal fun redactLogs(input: String): String = input.replace(URL_USERINFO, "$1REDACTED@").replace(URL_PARAM, "$1REDACTED")
