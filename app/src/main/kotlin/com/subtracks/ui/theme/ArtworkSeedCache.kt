package com.subtracks.ui.theme

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private const val SEED_ART_SIZE_PX = 128
private const val PREFETCH_SETTLE_MS = 250L
private const val MAX_CACHED_SEEDS = 256

object ArtworkSeedCache {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Deferred<Pair<Int, Int?>?>>()
    private val cache = LruCache<String, Pair<Int, Int?>>(MAX_CACHED_SEEDS)

    @Volatile
    private var store: ArtworkSeedStore? = null

    fun install(store: ArtworkSeedStore) {
        this.store = store
    }

    suspend fun seeds(
        context: Context,
        ref: CoverArtRef,
    ): Pair<Int, Int?>? {
        cache.get(ref.cacheKey)?.let { return it }
        val job = jobs.computeIfAbsent(ref.cacheKey) { scope.async { resolve(context, ref) } }
        val seeds = job.await()
        jobs.remove(ref.cacheKey, job)
        return seeds
    }

    fun prefetch(
        context: Context,
        ref: CoverArtRef,
    ) {
        scope.launch { seeds(context, ref) }
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
    ): Pair<Int, Int?>? =
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
            val image = (SingletonImageLoader.get(context).execute(request) as? SuccessResult)?.image
            image?.let { seedsFrom(it.toBitmap()) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
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
