package com.subtracks.data.db

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.StarType
import com.subtracks.data.sync.SyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Collections
import kotlin.math.max

@RunWith(AndroidJUnit4::class)
class QueryTimingMeasurementTest {
    private val executions: MutableList<SqlExecution> = Collections.synchronizedList(mutableListOf())
    private val timings = linkedMapOf<String, MutableList<Double>>()
    private lateinit var db: SubtracksDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val driver = LoggingSQLiteDriver(BundledSQLiteDriver()) { executions += it }
        db = Room.inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java).setDriver(driver).build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun measure() =
        runTest {
            val songCount = System.getProperty("measure.songs")?.toIntOrNull() ?: 20_000
            val items = items(songCount)
            db.sourcesDao().upsertSource(
                Source(id = 1, name = "bench", address = "http://localhost", isActive = true, createdAt = 0),
            )
            val source = FakeSource(items)

            executions.clear()
            val firstSyncMs = wall { SyncService(db, source).sync() }
            val firstSyncExecutions = executions.toList()

            executions.clear()
            val secondSyncMs = wall { SyncService(db, source).sync() }
            val secondSyncExecutions = executions.toList()

            executions.clear()
            measureReads()
            val readExecutions = executions.toList()

            val report =
                buildReport(
                    songCount = songCount,
                    firstSyncMs = firstSyncMs,
                    secondSyncMs = secondSyncMs,
                    firstSyncExecutions = firstSyncExecutions,
                    secondSyncExecutions = secondSyncExecutions,
                    readExecutions = readExecutions,
                )
            val out = File("build/query-timing.txt")
            out.parentFile?.mkdirs()
            out.writeText(report)
            print(report)
        }

    private suspend fun measureReads() {
        val dao = db.libraryDao()
        timed("albums first page (name)") { dao.albumsByName(1, 0, "").firstPage() }
        timed("albums first page (artist)") { dao.albumsByArtist(1, 0, "").firstPage() }
        timed("albums first page (year)") { dao.albumsByYear(1, 0, "").firstPage() }
        timed("albums first page (added)") { dao.albumsByRecentlyAdded(1, 0, "").firstPage() }
        timed("albums first page (starred)") { dao.albumsByStarred(1, 0, "").firstPage() }
        timed("albums first page (frequent)") { dao.albumsByFrequent(1, 0, "").firstPage() }
        timed("albums first page (recent)") { dao.albumsByRecent(1, 0, "").firstPage() }
        timed("artists first page (name)") { dao.artistsByName(1, 0, "").firstPage() }
        timed("artists first page (frequent)") { dao.artistsByFrequent(1, 0, "").firstPage() }
        timed("playlists first page (name)") { dao.playlistsByName(1, "").firstPage() }
        timed("albums deep page") { dao.albumsByName(1, 0, "").deepPage(1000) }
        timed("albums search FTS") { dao.albumsByName(1, 0, "Album 1").firstPage() }
        timed("albums search short") { dao.albumsByName(1, 0, "al").firstPage() }
        timed("songs search FTS") { dao.searchSongs(1, "Song", 50).first() }
        timed("home recently played") { dao.recentlyPlayedAlbums(1, 20).first() }
        timed("home most played") { dao.mostPlayedAlbums(1, 20).first() }
        timed("home recently added") { dao.recentlyAddedAlbums(1, 20).first() }
        timed("home recently starred") { dao.recentlyStarredSongs(1, 20).first() }
        timed("home genres") { dao.genresByMostPlayed(1).first() }
        timed("home decades") { dao.decades(1).first() }
        timed("recompute play data") { dao.recomputePlayData(1) }
    }

    private suspend fun <T : Any> PagingSource<Int, T>.firstPage(): List<T> =
        (
            load(
                PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
            ) as PagingSource.LoadResult.Page
        ).data

    private suspend fun <T : Any> PagingSource<Int, T>.deepPage(offset: Int): List<T> =
        (
            load(
                PagingSource.LoadParams.Append(key = offset, loadSize = 100, placeholdersEnabled = false),
            ) as PagingSource.LoadResult.Page
        ).data

    private suspend fun <T> timed(
        name: String,
        iterations: Int = 10,
        block: suspend () -> T,
    ) {
        runCatching { block() }
        val samples = timings.getOrPut(name) { mutableListOf() }
        repeat(iterations) {
            val start = System.nanoTime()
            val result = runCatching { block() }
            if (result.isSuccess) samples += (System.nanoTime() - start) / 1e6
        }
    }

    private suspend fun wall(block: suspend () -> Unit): Double {
        val start = System.nanoTime()
        block()
        return (System.nanoTime() - start) / 1e6
    }

    private fun buildReport(
        songCount: Int,
        firstSyncMs: Double,
        secondSyncMs: Double,
        firstSyncExecutions: List<SqlExecution>,
        secondSyncExecutions: List<SqlExecution>,
        readExecutions: List<SqlExecution>,
    ): String =
        buildString {
            appendLine("SQL query timing measurement")
            appendLine("songs=$songCount  albums=${songCount / 10}  artists=${max(1, songCount / 200)}")
            appendLine()
            appendLine("sync: first=%.1fms  second(no-op)=%.1fms".format(firstSyncMs, secondSyncMs))
            appendLine()
            appendLine("=== named measurements (ms) ===")
            appendLine("%-32s %6s %8s %8s %8s %8s".format("measurement", "n", "p50", "p95", "p99", "max"))
            timings.forEach { (name, samples) ->
                if (samples.isEmpty()) return@forEach
                val sorted = samples.sorted()
                appendLine(
                    "%-32s %6d %8.2f %8.2f %8.2f %8.2f".format(
                        name,
                        sorted.size,
                        pct(sorted, 50),
                        pct(sorted, 95),
                        pct(sorted, 99),
                        sorted.last(),
                    ),
                )
            }
            appendLine()
            appendLine("=== driver executions ===")
            appendLine(overThresholds("first sync", firstSyncExecutions))
            appendLine(overThresholds("second sync", secondSyncExecutions))
            appendLine(overThresholds("reads", readExecutions))
            appendLine()
            appendLine("=== slowest first-sync executions ===")
            firstSyncExecutions.sortedByDescending { it.durationMillis }.take(15).forEach {
                appendLine("  %8.2fms rows=%-6d %s".format(it.durationMillis, it.rows, it.sql.replace(Regex("\\s+"), " ").take(140)))
            }
            appendLine("=== slowest read executions ===")
            readExecutions.sortedByDescending { it.durationMillis }.take(15).forEach {
                appendLine("  %8.2fms rows=%-6d %s".format(it.durationMillis, it.rows, it.sql.replace(Regex("\\s+"), " ").take(140)))
            }
        }

    private fun overThresholds(
        label: String,
        records: List<SqlExecution>,
    ): String {
        val thresholds = listOf(10, 25, 50, 100, 250, 500)
        val counts = thresholds.joinToString { t -> ">${t}ms=${records.count { it.durationMillis > t }}" }
        return "$label: n=${records.size} $counts"
    }

    private fun pct(
        sorted: List<Double>,
        percentile: Int,
    ): Double = sorted[((percentile / 100.0) * (sorted.size - 1)).toInt()]

    internal data class Items(
        val artists: List<Artist>,
        val albums: List<Album>,
        val songs: List<Song>,
        val playlists: List<Playlist>,
        val playlistSongs: List<PlaylistSong>,
    )

    private fun items(songCount: Int): Items {
        val words = listOf("Midnight", "Abbey", "Blue", "Golden", "Silent", "Rolling", "Purple", "Electric")
        val artistCount = max(1, songCount / 200)
        val albumCount = max(1, songCount / 10)
        val artists =
            (0 until artistCount).map { i ->
                Artist(sourceId = 1, id = "ar$i", name = "Artist ${words[i % words.size]} $i", albumCount = 10, starred = null)
            }
        val albums =
            (0 until albumCount).map { i ->
                Album(
                    sourceId = 1,
                    id = "al$i",
                    artistId = "ar${i / 20}",
                    name = "${words[i % words.size]} Album $i",
                    albumArtist = "Artist ${i / 20}",
                    created = 1_000_000L + i,
                    coverArt = null,
                    genre = null,
                    year = (1950 + i % 60).toLong(),
                    starred = if (i % 11 == 0) 100L + i else null,
                    songCount = 10,
                )
            }
        val songs =
            (0 until songCount).map { i ->
                Song(
                    sourceId = 1,
                    id = "s$i",
                    albumId = "al${i / 10}",
                    artistId = "ar${i / 200}",
                    title = "${words[i % words.size]} Song $i",
                    album = "Album ${i / 10}",
                    artist = "Artist ${i / 200}",
                    duration = 200,
                    track = (i % 10 + 1).toLong(),
                    disc = 1,
                    starred = if (i % 7 == 0) 1000L + i else null,
                    genre = null,
                    created = 1_000_000L + i,
                    playCount = (i % 13).toLong(),
                    played = if (i % 5 == 0) 1_700_000_000_000L + i else null,
                    genres = listOf("genre${i % 50}"),
                )
            }
        val playlistCount = max(1, songCount / 100)
        val playlists =
            (0 until playlistCount).map { i ->
                Playlist(
                    sourceId = 1,
                    id = "pl$i",
                    name = "Playlist $i",
                    comment = null,
                    coverArt = null,
                    songCount = 20,
                    created = 1_000_000L + i,
                    changed = 2_000_000L + i,
                    duration = 3600,
                )
            }
        val playlistSongs =
            (0 until playlistCount).flatMap { p ->
                (0 until 20).map { s -> PlaylistSong(1, "pl$p", "s${(p * 20 + s) % songCount}", s.toLong()) }
            }
        return Items(artists, albums, songs, playlists, playlistSongs)
    }
}

private class FakeSource(
    private val items: QueryTimingMeasurementTest.Items,
    private val batchSize: Int = 500,
) : MusicSource {
    override val id: Long = 1

    override suspend fun ping() = Unit

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) = Unit

    override suspend fun scrobble(
        songId: String,
        submission: Boolean,
        time: Long?,
    ) = Unit

    override fun artists(): Flow<List<Artist>> = batches(items.artists)

    override fun albums(): Flow<List<Album>> = batches(items.albums)

    override fun songs(): Flow<List<Song>> = batches(items.songs)

    override fun playlists(): Flow<List<Playlist>> = batches(items.playlists)

    override fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>> = batches(items.playlistSongs)

    private fun <T> batches(list: List<T>): Flow<List<T>> =
        flow {
            list.chunked(batchSize).forEach { emit(it) }
        }
}
