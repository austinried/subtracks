package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicConfig
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.source.subsonic.SubsonicClient
import com.subtracks.data.sync.SyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import com.subtracks.data.source.subsonic.SubsonicSource as SubsonicMusicSource

class SourceRepository(
    private val db: SubtracksDatabase,
    private val http: OkHttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var active: SubsonicMusicSource? = null

    init {
        scope.launch {
            db.sourcesDao().activeSubsonicConfig().collect { config ->
                active = config?.toMusicSource()
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    fun sources(): Flow<List<Source>> = db.sourcesDao().sources()

    fun activeSourceId(): Flow<Long?> = db.sourcesDao().activeSourceId()

    suspend fun activeSourceIdOnce(): Long? = db.sourcesDao().activeSourceIdOnce()

    fun activeConfig(): Flow<SubsonicConfig?> = db.sourcesDao().activeSubsonicConfig()

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean = false,
    ): CoverArtRef? {
        val source = active ?: return null
        val url = source.coverArtUri(coverArt, thumbnail)?.toString() ?: return null
        return CoverArtRef(url = url, cacheKey = "${source.id}:$coverArt:$thumbnail")
    }

    fun streamUri(songId: String): String? = active?.streamUri(songId)?.toString()

    suspend fun addSource(
        name: String,
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
    ): Long {
        val dao = db.sourcesDao()
        val id =
            dao.insertSource(
                Source(
                    name = name.trim(),
                    address = normalizeAddress(address),
                    isActive = false,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        dao.upsertSubsonicSource(
            SubsonicSource(
                sourceId = id,
                username = username.trim(),
                password = password,
                useTokenAuth = useTokenAuth,
            ),
        )
        dao.setActiveSource(id)
        return id
    }

    suspend fun selectSource(id: Long) = db.sourcesDao().setActiveSource(id)

    suspend fun deleteSource(id: Long) {
        val dao = db.sourcesDao()
        val wasActive = dao.activeSourceIdOnce() == id
        dao.deleteSource(id)
        if (wasActive) {
            dao.firstSourceId()?.let { dao.setActiveSource(it) }
        }
    }

    suspend fun ping(
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { client(address, username, password, useTokenAuth).get("ping") }.map { }
        }

    suspend fun sync(): Result<Unit> =
        runCatching {
            val config = db.sourcesDao().activeSubsonicConfigOnce() ?: error("No server configured")
            SyncService(db, config.toMusicSource()).sync()
        }

    private fun SubsonicConfig.toMusicSource(): SubsonicMusicSource =
        SubsonicMusicSource(id, client(address, username, password, useTokenAuth))

    private fun client(
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
    ) = SubsonicClient(
        baseUrl = normalizeAddress(address).toHttpUrl(),
        username = username,
        password = password,
        useTokenAuth = useTokenAuth,
        http = http,
    )

    private fun normalizeAddress(address: String): String {
        val trimmed = address.trim()
        val withScheme =
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "http://$trimmed"
            }
        return withScheme.trimEnd('/') + "/"
    }
}
