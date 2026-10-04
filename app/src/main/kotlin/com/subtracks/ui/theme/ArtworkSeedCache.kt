package com.subtracks.ui.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.get
import androidx.palette.graphics.Palette
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.subtracks.data.model.ArtworkSeed
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.prefs.ArtworkSeedValue
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.ArtworkSeedStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

private const val SEED_ART_SIZE_PX = 128
private const val PREFETCH_SETTLE_MS = 250L
private const val MAX_CACHED_SEEDS = 256
private const val BUSY_BAND_TOP = 0.72f
private const val BUSY_BRIGHT_LUMINANCE = 0.6f
private const val BUSY_BRIGHT_FRACTION = 0.05f
private const val BUSY_EDGE_LUMINANCE = 0.2f
private const val BUSY_EDGE_FRACTION = 0.1f

private data class Seed(
    val primary: Int,
    val secondary: Int?,
    val busy: Boolean?,
)

object ArtworkSeedCache {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Deferred<Pair<Int, Int?>?>>()
    private val cache = LruCache<String, Seed>(MAX_CACHED_SEEDS)

    @Volatile
    private var store: ArtworkSeedStore? = null

    @Volatile
    private var prefs: UserPreferences? = null

    @Volatile
    private var lastSeed: ArtworkSeedValue? = null

    fun install(
        store: ArtworkSeedStore?,
        prefs: UserPreferences? = null,
    ) {
        this.store = store
        this.prefs = prefs
        if (prefs == null) {
            lastSeed = null
            return
        }
        scope.launch {
            ignoreFailure { prefs.lastSeed() }?.let { lastSeed = it }
        }
    }

    fun cached(cacheKey: String): Pair<Int, Int?>? =
        cache.get(cacheKey)?.let { it.primary to it.secondary }
            ?: lastSeed?.takeIf { it.cacheKey == cacheKey }?.let { it.primary to it.secondary }

    fun markLast(ref: CoverArtRef) {
        val prefs = prefs ?: return
        val seed = cache.get(ref.cacheKey) ?: return
        val value = ArtworkSeedValue(ref.cacheKey, seed.primary, seed.secondary)
        lastSeed = value
        scope.launch { ignoreFailure { prefs.setLastSeed(value) } }
    }

    fun cachedBusy(cacheKey: String): Boolean? = cache.get(cacheKey)?.busy

    suspend fun seeds(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? {
        cache.get(ref.cacheKey)?.let { return it.primary to it.secondary }
        val job = jobs.computeIfAbsent(ref.cacheKey) { scope.async { resolve(context, ref) } }
        job.invokeOnCompletion { jobs.remove(ref.cacheKey, job) }
        return job.await()
    }

    fun prefetch(
        context: Context,
        ref: CoverArtRef,
    ) {
        scope.launch { seeds(context, ref) }
    }

    suspend fun overlaidNameBusy(
        context: Context,
        ref: CoverArtRef,
    ): Boolean {
        cache.get(ref.cacheKey)?.busy?.let { return it }
        seeds(context, ref)
        return cache.get(ref.cacheKey)?.busy ?: false
    }

    @VisibleForTesting
    fun clear() {
        jobs.clear()
        cache.evictAll()
    }

    private suspend fun resolve(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? {
        val stored = ignoreFailure { store?.seed(ref.cacheKey) }
        val storedSeeds = stored?.let { it.primary to it.secondary }
        if (stored != null) {
            cache.put(ref.cacheKey, Seed(stored.primary, stored.secondary, stored.nameBusy))
            if (stored.nameBusy != null) return storedSeeds
        }
        val bitmap = decode(context, ref) ?: return storedSeeds
        val seeds = seedsFrom(bitmap) ?: return storedSeeds
        val nameBusy = bottomBandBusy(bitmap)
        cache.put(ref.cacheKey, Seed(seeds.first, seeds.second, nameBusy))
        ignoreFailure { store?.save(ArtworkSeed(ref.cacheKey, seeds.first, seeds.second, nameBusy)) }
        return seeds
    }

    private suspend fun <T> ignoreFailure(block: suspend () -> T): T? =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    private suspend fun decode(
        context: Context,
        ref: CoverArtRef,
    ): Bitmap? =
        try {
            val request =
                ImageRequest
                    .Builder(context)
                    .data(ref.url)
                    .memoryCacheKey(ref.cacheKey)
                    .diskCacheKey(ref.cacheKey)
                    .size(SEED_ART_SIZE_PX)
                    .allowHardware(false)
                    .build()
            (SingletonImageLoader.get(context).execute(request) as? SuccessResult)?.image?.toBitmap()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    @VisibleForTesting
    internal fun bottomBandBusy(bitmap: Bitmap): Boolean {
        if (bitmap.width == 0 || bitmap.height == 0) return false
        val firstRow = (bitmap.height * BUSY_BAND_TOP).toInt().coerceIn(0, bitmap.height - 1)
        val width = bitmap.width
        var previousRow: FloatArray? = null
        var bright = 0
        var edges = 0
        var count = 0
        for (y in firstRow until bitmap.height) {
            val row = FloatArray(width) { x -> lumaAt(bitmap, x, y) }
            for (x in 0 until width) {
                val luminance = row[x]
                if (luminance > BUSY_BRIGHT_LUMINANCE) bright++
                if (x > 0 && abs(luminance - row[x - 1]) > BUSY_EDGE_LUMINANCE) edges++
                if (previousRow != null && abs(luminance - previousRow[x]) > BUSY_EDGE_LUMINANCE) edges++
                count++
            }
            previousRow = row
        }
        if (count == 0) return false
        return bright.toFloat() / count > BUSY_BRIGHT_FRACTION || edges.toFloat() / count > BUSY_EDGE_FRACTION
    }

    private fun lumaAt(
        bitmap: Bitmap,
        x: Int,
        y: Int,
    ): Float {
        val color = Color(bitmap[x, y])
        return 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    }

    private fun seedsFrom(bitmap: Bitmap): Pair<Int, Int?>? {
        val palette = Palette.from(bitmap).generate()
        val primary =
            palette.vibrantSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.darkVibrantSwatch
                ?: palette.mutedSwatch
                ?: palette.dominantSwatch
                ?: return null
        val primaryColor = Color(primary.rgb or 0xFF000000.toInt())
        val secondary =
            listOfNotNull(
                palette.vibrantSwatch,
                palette.lightVibrantSwatch,
                palette.darkVibrantSwatch,
                palette.mutedSwatch,
                palette.lightMutedSwatch,
                palette.darkMutedSwatch,
                palette.dominantSwatch,
            ).map { it.rgb }
                .distinct()
                .filter { it != primary.rgb }
                .maxByOrNull { distinctness(primaryColor, Color(it or 0xFF000000.toInt())) }
        return primary.rgb to secondary
    }

    private fun distinctness(
        a: Color,
        b: Color,
    ): Float {
        val ha = a.toHsl().first
        val hb = b.toHsl().first
        val hueDistance = minOf(abs(ha - hb), 360f - abs(ha - hb)) / 180f
        return hueDistance + abs(a.luminance() - b.luminance()) * 2f
    }
}

@Composable
fun PrefetchArtworkSeeds(ref: CoverArtRef?) {
    val context = LocalPlatformContext.current
    LaunchedEffect(ref?.cacheKey) {
        if (ref == null) return@LaunchedEffect
        delay(PREFETCH_SETTLE_MS)
        ArtworkSeedCache.prefetch(context, ref)
    }
}

@Composable
fun rememberOverlaidNameBusy(ref: CoverArtRef?): Boolean {
    val context = LocalPlatformContext.current
    val key = ref?.cacheKey
    var busy by remember(key) { mutableStateOf(key?.let(ArtworkSeedCache::cachedBusy) ?: false) }
    LaunchedEffect(key) {
        busy = ref?.let { ArtworkSeedCache.overlaidNameBusy(context, it) } ?: false
    }
    return busy
}
