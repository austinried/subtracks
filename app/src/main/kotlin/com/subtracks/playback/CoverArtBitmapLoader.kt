package com.subtracks.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val ARTWORK_SIZE = 512

object CoverArtArtwork {
    const val SCHEME = "subtracks-cover"

    fun uri(coverArtId: String): Uri =
        Uri
            .Builder()
            .scheme(SCHEME)
            .authority("cover")
            .appendPath(coverArtId)
            .build()

    fun coverArtId(uri: Uri): String? = if (uri.scheme == SCHEME) uri.lastPathSegment?.takeIf { it.isNotEmpty() } else null
}

@OptIn(UnstableApi::class)
class CoverArtBitmapLoader(
    private val context: Context,
    private val sourceRepository: SourceRepository,
    private val imageLoader: ImageLoader,
) : BitmapLoader {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun supportsMimeType(mimeType: String): Boolean = mimeType.startsWith("image/")

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val coverArtId =
            CoverArtArtwork.coverArtId(uri)
                ?: return Futures.immediateFailedFuture(IllegalArgumentException("Unsupported artwork URI: $uri"))
        val ref =
            sourceRepository.coverArt(coverArtId, thumbnail = true)
                ?: return Futures.immediateFailedFuture(IllegalStateException("No active source for artwork"))
        val future = SettableFuture.create<Bitmap>()
        val request =
            ImageRequest
                .Builder(context)
                .data(ref.url)
                .memoryCacheKey(ref.cacheKey)
                .diskCacheKey(ref.cacheKey)
                .size(ARTWORK_SIZE)
                .allowHardware(false)
                .build()
        scope.launch {
            try {
                val bitmap = (imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
                if (bitmap == null) {
                    future.setException(IllegalStateException("No bitmap for ${ref.cacheKey}"))
                } else {
                    future.set(bitmap)
                }
            } catch (e: Exception) {
                future.setException(e)
            }
        }
        return future
    }

    override fun loadBitmapFromMetadata(metadata: MediaMetadata): ListenableFuture<Bitmap> {
        metadata.artworkData?.let { return decodeBitmap(it) }
        metadata.artworkUri?.let { return loadBitmap(it) }
        return Futures.immediateFailedFuture(IllegalArgumentException("Artwork has no data or URI"))
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> =
        try {
            BitmapFactory
                .decodeByteArray(data, 0, data.size)
                ?.let { Futures.immediateFuture(it) }
                ?: Futures.immediateFailedFuture(IllegalArgumentException("Could not decode artwork"))
        } catch (e: Exception) {
            Futures.immediateFailedFuture(e)
        }
}
