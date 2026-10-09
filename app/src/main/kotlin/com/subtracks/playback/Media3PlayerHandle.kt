package com.subtracks.playback

import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.subtracks.R
import com.subtracks.UiMessage
import com.subtracks.data.model.AudioEncoding
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.log.Log

class Media3PlayerHandle(
    private val controller: MediaController,
    private val sourceRepository: SourceRepository,
    private val downloads: DownloadRepository,
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

    @get:UnstableApi
    override val audioEncoding: AudioEncoding?
        get() =
            controller.currentTracks.groups
                .firstOrNull { it.type == C.TRACK_TYPE_AUDIO && it.isSelected }
                ?.let { group ->
                    (0 until group.length)
                        .firstOrNull { group.isTrackSelected(it) }
                        ?.let(group::getTrackFormat)
                }?.let { format ->
                    AudioEncoding(
                        mimeType = format.sampleMimeType,
                        bitrate = format.bitrate.takeIf { it != Format.NO_VALUE },
                        sampleRate = format.sampleRate.takeIf { it != Format.NO_VALUE },
                        channels = format.channelCount.takeIf { it != Format.NO_VALUE },
                    )
                }

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

    override fun itemUris(): List<String?> =
        (0 until controller.mediaItemCount).map {
            controller
                .getMediaItemAt(it)
                .localConfiguration
                ?.uri
                ?.toString()
        }

    override fun replaceItem(
        index: Int,
        item: QueueItem,
    ) = controller.replaceMediaItem(index, toMediaItem(item))

    override fun seekToIndex(index: Int) {
        Log.d(
            "playback",
            "seekToIndex($index) position=${controller.currentPosition} duration=${duration(
                controller.duration,
            )} state=${controller.playbackState}",
        )
        controller.seekTo(index, 0)
    }

    override fun seekTo(positionMs: Long) {
        Log.d(
            "playback",
            "seekTo(${positionMs}ms) from=${controller.currentPosition} duration=${duration(controller.duration)} " +
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
                    Log.i("playback", "${describe(mediaItem)} transition=${transitionReason(reason)}")
                    listener.onTransition()
                }

                override fun onEvents(
                    player: Player,
                    events: Player.Events,
                ) = listener.onEvents()

                override fun onPlayerError(error: PlaybackException) {
                    Log.w(
                        "playback",
                        "error ${error.errorCodeName} ${describe(controller.currentMediaItem)} " +
                            "position=${controller.currentPosition} duration=${duration(controller.duration)}",
                        error,
                    )
                    listener.onError(playbackErrorMessage(error))
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(
                        "playback",
                        "state=$playbackState position=${controller.currentPosition} " +
                            "duration=${duration(controller.duration)} itemDuration=${currentItem?.durationMs}",
                    )
                    if (playbackState == Player.STATE_ENDED) {
                        Log.i("playback", "ended ${describe(controller.currentMediaItem)}")
                    }
                }
            },
        )
    }

    private fun describe(mediaItem: MediaItem?): String {
        if (mediaItem == null) return "item=none"
        val metadata = mediaItem.mediaMetadata
        val artist =
            metadata.artist
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?.let { " artist=\"$it\"" }
                .orEmpty()
        val album =
            metadata.albumTitle
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?.let { " album=\"$it\"" }
                .orEmpty()
        val source =
            when (mediaItem.localConfiguration?.uri?.scheme) {
                "file", "content" -> "file"
                "http", "https" -> "http"
                null -> "none"
                else -> "other"
            }
        return "item id=${mediaItem.mediaId} title=\"${metadata.title?.toString().orEmpty()}\"$artist$album source=$source"
    }

    private fun duration(value: Long): String = if (value == C.TIME_UNSET) "unset" else "${value}ms"

    private fun transitionReason(reason: Int): String =
        when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "repeat"
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "auto"
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "seek"
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> "playlist"
            else -> reason.toString()
        }

    private fun toMediaItem(item: QueueItem): MediaItem {
        val quality = sourceRepository.quality.value
        return queueMediaItem(
            item = item,
            local = downloads.localUri(item.id),
            stream = sourceRepository.streamUri(item.id, item.durationMs, quality),
            transcode = quality.transcodes,
        )
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
}

private const val EXTRA_COVER_ART_ID = "coverArtId"
private const val EXTRA_DURATION_MS = "durationMs"

internal fun queueMediaItem(
    item: QueueItem,
    local: String?,
    stream: String?,
    transcode: Boolean,
): MediaItem {
    val metadata =
        MediaMetadata
            .Builder()
            .setTitle(item.title)
            .setArtist(item.artist)
            .setAlbumTitle(item.album)
            .setArtworkUri(item.coverArtId?.takeIf { it.isNotEmpty() }?.let(CoverArtArtwork::uri))
            .setDurationMs(item.durationMs)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(
                Bundle().apply {
                    putString(EXTRA_COVER_ART_ID, item.coverArtId)
                    item.durationMs?.let { putLong(EXTRA_DURATION_MS, it) }
                },
            ).build()
    val builder =
        MediaItem
            .Builder()
            .setMediaId(item.id)
            .setUri(local ?: stream)
            .setMediaMetadata(metadata)
    if (local == null && transcode) {
        item.durationMs?.takeIf { it > 0 }?.let { duration ->
            builder.setClippingConfiguration(
                ClippingConfiguration.Builder().setEndPositionMs(duration).build(),
            )
        }
    }
    return builder.build()
}

internal fun playbackErrorMessage(error: PlaybackException): UiMessage =
    when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        -> UiMessage(R.string.playback_error_network)

        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> UiMessage(R.string.playback_error_refused)

        else -> UiMessage(R.string.playback_error, listOf(error.errorCodeName))
    }
