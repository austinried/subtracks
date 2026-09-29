package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicConfig
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.model.coverArtKey
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.declaredStreamLength
import com.subtracks.data.source.streamLengthSuffix
import com.subtracks.data.source.subsonic.SubsonicClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import com.subtracks.data.source.subsonic.SubsonicSource as SubsonicMusicSource

class SourceRepository(
    private val db: SubtracksDatabase,
    private val http: OkHttpClient,
    private val prefs: UserPreferences,
    private val artworkStore: ArtworkStore,
    networkMode: Flow<NetworkMode> = flowOf(NetworkMode.Wifi),
    private val showMessage: (String) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var active: SubsonicMusicSource? = null

    @Volatile
    private var activeSourceId: Long? = null

    private val _quality = MutableStateFlow(StreamQuality())
    val quality: StateFlow<StreamQuality> = _quality

    @Volatile
    private var downloadQuality = StreamQuality()

    @Volatile
    private var allowMeteredDownloads = false

    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline

    fun downloadsAllowedOverMetered(): Boolean = allowMeteredDownloads

    fun setOfflineMode(enabled: Boolean) {
        scope.launch { prefs.setOfflineMode(enabled) }
    }

    init {
        scope.launch {
            prefs.downloadQuality().collect { downloadQuality = it }
        }
        scope.launch {
            prefs.downloadOverMetered().collect { allowMeteredDownloads = it }
        }
        scope.launch {
            prefs.offlineMode().collect { _offline.value = it }
        }
        scope.launch {
            // Read the persisted flag before the source config can build a source or probe it, so a
            // cold start with offline on never touches the network.
            _offline.value = prefs.offlineMode().first()
            combine(
                db.sourcesDao().activeSubsonicConfig(),
                networkMode,
                prefs.streamQuality(NetworkMode.Wifi),
                prefs.streamQuality(NetworkMode.Mobile),
                prefs.syncConcurrency(),
            ) { config, mode, wifi, mobile, concurrency ->
                Triple(config, if (mode == NetworkMode.Wifi) wifi else mobile, concurrency)
            }.collect { (config, quality, concurrency) ->
                active = config?.toMusicSource(quality, concurrency)
                _quality.value = quality
                val sourceChanged = config?.id != activeSourceId
                activeSourceId = config?.id
                if (sourceChanged && config != null && config.useTokenAuth && !_offline.value) {
                    scope.launch { runCatching { config.toClient().check("ping") } }
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

    suspend fun sourceConfigOnce(id: Long): SubsonicConfig? = db.sourcesDao().subsonicConfigOnce(id)

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean = false,
    ): CoverArtRef? {
        val source = active ?: return null
        if (coverArt == null) return null
        val cacheKey = coverArtKey(source.id, coverArt, thumbnail)
        artworkStore.uri(source.id, cacheKey)?.let { return CoverArtRef(url = it, cacheKey = cacheKey) }
        if (_offline.value) return null
        val url = source.coverArtUri(coverArt, thumbnail)?.toString() ?: return null
        return CoverArtRef(url = url, cacheKey = cacheKey)
    }

    fun networkCoverArt(
        sourceId: Long,
        coverArt: String,
        thumbnail: Boolean,
    ): String? {
        if (_offline.value) return null
        val source = active ?: return null
        if (source.id != sourceId) return null
        return source.coverArtUri(coverArt, thumbnail)?.toString()
    }

    fun streamUri(
        songId: String,
        durationMs: Long?,
        quality: StreamQuality,
    ): String? {
        if (_offline.value) return null
        val uri = active?.streamUri(songId)?.toString() ?: return null
        val length = declaredStreamLength(durationMs, quality) ?: return uri
        return uri + streamLengthSuffix(length)
    }

    fun downloadUri(
        sourceId: Long,
        songId: String,
    ): String? {
        if (_offline.value) return null
        val source = active?.takeIf { activeSourceId == sourceId } ?: return null
        // A transcode preference downloads the stream, so the saved file is already the wanted
        // quality rather than the original that would then have to be transcoded on every play.
        val quality = downloadQuality
        return if (quality.transcodes) {
            source.streamUri(songId, quality.maxBitrate, quality.format).toString()
        } else {
            source.downloadUri(songId).toString()
        }
    }

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

    suspend fun updateSource(
        id: Long,
        name: String,
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
    ) {
        val dao = db.sourcesDao()
        val source = dao.sourceOnce(id) ?: return
        dao.upsertSource(source.copy(name = name.trim(), address = normalizeAddress(address)))
        dao.upsertSubsonicSource(
            SubsonicSource(
                sourceId = id,
                username = username.trim(),
                password = password,
                useTokenAuth = useTokenAuth,
            ),
        )
    }

    suspend fun deleteSource(id: Long): Boolean {
        if (db.sourcesDao().activeSourceIdOnce() == id) return false
        db.sourcesDao().deleteSource(id)
        return true
    }

    suspend fun ping(
        address: String,
        username: String,
        password: String,
        useTokenAuth: Boolean,
    ): Result<Boolean> =
        if (_offline.value) {
            Result.failure(IllegalStateException("Offline mode is on"))
        } else {
            withContext(Dispatchers.IO) {
                var fellBack = false
                runCatching {
                    client(address, username, password, useTokenAuth) { fellBack = true }.check("ping")
                }.map { fellBack }
            }
        }

    suspend fun activeMusicSource(): MusicSource? {
        if (_offline.value) return null
        val config = db.sourcesDao().activeSubsonicConfigOnce() ?: return null
        return config.toMusicSource(quality.value, prefs.syncConcurrency().first())
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
        quality: StreamQuality,
        syncConcurrency: Int,
    ): SubsonicMusicSource =
        SubsonicMusicSource(
            id = id,
            client = toClient(),
            maxBitrate = quality.maxBitrate,
            streamFormat = quality.format,
            maxConcurrentFetches = syncConcurrency,
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
