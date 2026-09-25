package com.subtracks.ui.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.subtracks.data.model.CoverArtRef
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
import kotlin.math.sqrt

private const val SEED_ART_SIZE_PX = 128
private const val PREFETCH_SETTLE_MS = 250L
private const val MAX_CACHED_SEEDS = 256
private const val BUSY_BAND_TOP = 0.72f
private const val BUSY_MEAN_LUMINANCE = 0.52f
private const val BUSY_STDDEV_LUMINANCE = 0.16f

object ArtworkSeedCache {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Deferred<Pair<Int, Int?>?>>()
    private val cache = LruCache<String, Pair<Int, Int?>>(MAX_CACHED_SEEDS)
    private val busy = LruCache<String, Boolean>(MAX_CACHED_SEEDS)

    @Volatile
    private var store: ArtworkSeedStore? = null

    fun install(store: ArtworkSeedStore) {
        this.store = store
    }

    fun cached(cacheKey: String): Pair<Int, Int?>? = cache.get(cacheKey)

    suspend fun seeds(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? {
        cache.get(ref.cacheKey)?.let { return it }
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
        busy.get(ref.cacheKey)?.let { return it }
        val result = decode(context, ref)?.let(::bottomBandBusy) ?: false
        busy.put(ref.cacheKey, result)
        return result
    }

    @VisibleForTesting
    fun clear() {
        jobs.clear()
        cache.evictAll()
        busy.evictAll()
    }

    private suspend fun resolve(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? {
        ignoreFailure { store?.seed(ref.cacheKey) }?.let {
            cache.put(ref.cacheKey, it)
            return it
        }
        val seeds = extract(context, ref) ?: return null
        cache.put(ref.cacheKey, seeds)
        ignoreFailure { store?.save(ref.cacheKey, seeds) }
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

    private suspend fun extract(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? = decode(context, ref)?.let(::seedsFrom)

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

    // ponytail: whole-band mean/variance heuristic, swap for an edge-density pass if it misfires
    @VisibleForTesting
    internal fun bottomBandBusy(bitmap: Bitmap): Boolean {
        val firstRow = (bitmap.height * BUSY_BAND_TOP).toInt().coerceIn(0, bitmap.height - 1)
        var sum = 0f
        var sumSquares = 0f
        var count = 0
        for (y in firstRow until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val luminance = Color(bitmap.getPixel(x, y)).luminance()
                sum += luminance
                sumSquares += luminance * luminance
                count++
            }
        }
        if (count == 0) return false
        val mean = sum / count
        val deviation = sqrt((sumSquares / count - mean * mean).coerceAtLeast(0f))
        return mean > BUSY_MEAN_LUMINANCE || deviation > BUSY_STDDEV_LUMINANCE
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
    return produceState(false, ref?.cacheKey) {
        value = ref?.let { ArtworkSeedCache.overlaidNameBusy(context, it) } ?: false
    }.value
}
