package com.subtracks.playback

import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.subtracks.data.repo.SourceRepository

class Media3PlayerHandle(
    private val controller: MediaController,
    private val sourceRepository: SourceRepository,
) : PlayerHandle {
    override val itemCount: Int get() = controller.mediaItemCount

    override val currentIndex: Int get() = controller.currentMediaItemIndex

    override val isIdle: Boolean get() = controller.playbackState == Player.STATE_IDLE

    override val isEnded: Boolean get() = controller.playbackState == Player.STATE_ENDED

    override val isBuffering: Boolean get() = controller.playbackState == Player.STATE_BUFFERING

    override val isPlaying: Boolean get() = controller.isPlaying

    override val playWhenReady: Boolean get() = controller.playWhenReady

    override val durationMs: Long
        get() = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0

    override val currentPositionMs: Long get() = controller.currentPosition.coerceAtLeast(0)

    override val currentItem: QueueItem? get() = controller.currentMediaItem?.let(::toQueueItem)

    override fun setWindow(
        items: List<QueueItem>,
        startIndex: Int,
        startPositionMs: Long,
    ) = controller.setMediaItems(items.map(::toMediaItem), startIndex, startPositionMs)

    override fun addFirst(item: QueueItem) = controller.addMediaItems(0, listOf(toMediaItem(item)))

    override fun addLast(item: QueueItem) = controller.addMediaItems(controller.mediaItemCount, listOf(toMediaItem(item)))

    override fun insertAt(
        index: Int,
        item: QueueItem,
    ) = controller.addMediaItems(index, listOf(toMediaItem(item)))

    override fun removeFirst() = controller.removeMediaItem(0)

    override fun removeLast() = controller.removeMediaItem(controller.mediaItemCount - 1)

    override fun removeAt(index: Int) = controller.removeMediaItem(index)

    override fun move(
        from: Int,
        to: Int,
    ) = controller.moveMediaItem(from, to)

    override fun prepare() = controller.prepare()

    override fun play() = controller.play()

    override fun pause() = controller.pause()

    override fun stop() = controller.stop()

    override fun clear() = controller.clearMediaItems()

    override fun seekToIndex(index: Int) {
        Log.d(TAG, "seekToIndex($index) playerDuration=${controller.duration} state=${controller.playbackState}")
        controller.seekTo(index, 0)
    }

    override fun seekTo(positionMs: Long) {
        Log.d(
            TAG,
            "seekTo(${positionMs}ms) from=${controller.currentPosition} playerDuration=${controller.duration} " +
                "itemDuration=${currentItem?.durationMs} state=${controller.playbackState}",
        )
        controller.seekTo(positionMs)
    }

    override fun setRepeatOne(enabled: Boolean) {
        controller.repeatMode = if (enabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    override fun addListener(listener: PlayerHandle.Listener) {
        controller.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    Log.d(TAG, "item=${mediaItem?.mediaId}")
                    listener.onTransition()
                }

                override fun onEvents(
                    player: Player,
                    events: Player.Events,
                ) = listener.onEvents()

                override fun onPlayerError(error: PlaybackException) {
                    Log.w(
                        TAG,
                        "Playback error ${error.errorCodeName} at ${controller.currentPosition}/${controller.duration}",
                        error,
                    )
                    listener.onError(error.errorCodeName)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(
                        TAG,
                        "state=$playbackState position=${controller.currentPosition} " +
                            "playerDuration=${controller.duration} itemDuration=${currentItem?.durationMs}",
                    )
                }
            },
        )
    }

    private fun toMediaItem(item: QueueItem): MediaItem {
        val metadata =
            MediaMetadata
                .Builder()
                .setTitle(item.title)
                .setArtist(item.artist)
                .setAlbumTitle(item.album)
                .setArtworkUri(sourceRepository.coverArt(item.coverArtId, true)?.url?.toUri())
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .setExtras(
                    Bundle().apply {
                        putString(EXTRA_COVER_ART_ID, item.coverArtId)
                        item.durationMs?.let { putLong(EXTRA_DURATION_MS, it) }
                    },
                ).build()
        return MediaItem
            .Builder()
            .setMediaId(item.id)
            .setUri(sourceRepository.streamUri(item.id))
            .setMediaMetadata(metadata)
            .build()
    }

    private fun toQueueItem(mediaItem: MediaItem): QueueItem =
        QueueItem(
            id = mediaItem.mediaId,
            title =
                mediaItem.mediaMetadata.title
                    ?.toString()
                    .orEmpty(),
            artist = mediaItem.mediaMetadata.artist?.toString(),
            album = mediaItem.mediaMetadata.albumTitle?.toString(),
            coverArtId = mediaItem.mediaMetadata.extras?.getString(EXTRA_COVER_ART_ID),
            durationMs =
                mediaItem.mediaMetadata.extras
                    ?.takeIf { it.containsKey(EXTRA_DURATION_MS) }
                    ?.getLong(EXTRA_DURATION_MS),
        )

    private companion object {
        const val TAG = "SubtracksPlayback"
        const val EXTRA_COVER_ART_ID = "coverArtId"
        const val EXTRA_DURATION_MS = "durationMs"
    }
}
