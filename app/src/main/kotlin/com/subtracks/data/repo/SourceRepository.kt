package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicConfig
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.subsonic.SubsonicClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import com.subtracks.data.source.subsonic.SubsonicSource as SubsonicMusicSource

class SourceRepository(
    private val db: SubtracksDatabase,
    private val http: OkHttpClient,
    private val prefs: UserPreferences,
    private val showMessage: (String) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var active: SubsonicMusicSource? = null
    private var probedTokenAuthId: Long? = null

    init {
        scope.launch {
            combine(
                db.sourcesDao().activeSubsonicConfig(),
                prefs.maxBitrate,
                prefs.streamFormat,
            ) { config, maxBitrate, streamFormat -> Triple(config, maxBitrate, streamFormat) }
                .collect { (config, maxBitrate, streamFormat) ->
                    active = config?.toMusicSource(maxBitrate, streamFormat)
                    config
                        ?.takeIf { it.useTokenAuth && it.id != probedTokenAuthId }
                        ?.let { source ->
                            probedTokenAuthId = source.id
                            scope.launch { runCatching { source.toClient().check("ping") } }
                        }
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
    ): Result<Boolean> =
        withContext(Dispatchers.IO) {
            var fellBack = false
            runCatching {
                client(address, username, password, useTokenAuth) { fellBack = true }.check("ping")
            }.map { fellBack }
        }

    suspend fun activeMusicSource(): MusicSource? {
        val config = db.sourcesDao().activeSubsonicConfigOnce() ?: return null
        return config.toMusicSource(prefs.maxBitrate.first(), prefs.streamFormat.first())
    }

    private fun SubsonicConfig.toClient(): SubsonicClient =
        client(
            address = address,
            username = username,
            password = password,
            useTokenAuth = useTokenAuth,
            onTokenAuthUnsupported = { disableTokenAuth(id) },
        )

    private fun SubsonicConfig.toMusicSource(
        maxBitrate: Int,
        streamFormat: String?,
    ): SubsonicMusicSource =
        SubsonicMusicSource(
            id = id,
            client = toClient(),
            maxBitrate = maxBitrate,
            streamFormat = streamFormat,
        )

    private fun disableTokenAuth(sourceId: Long) {
        scope.launch {
            db.sourcesDao().disableTokenAuth(sourceId)
            showMessage("Server does not support token auth; using the password instead")
        }
    }

    private fun client(
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
        onTokenAuthUnsupported: () -> Unit = {},
    ) = SubsonicClient(
        baseUrl = normalizeAddress(address).toHttpUrl(),
        username = username,
        password = password,
        useTokenAuth = useTokenAuth,
        http = http,
        onTokenAuthUnsupported = onTokenAuthUnsupported,
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
