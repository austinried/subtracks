package com.subtracks.playback

import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * Reports a track whose duration is known from its metadata as finite rather than live.
 *
 * Media3 marks a progressive stream live when the data source reports no length and the extractor
 * no duration. The app declares a length only for a transcode of a track that has one in a
 * length-estimable format, so a chunked response stays live for an Opus/Ogg/Vorbis transcode, for
 * a transcode of a track whose library duration is missing, and for a direct play the server does
 * not give a Content-Length. A live item makes the media session publish no playback position, so
 * the notification loses its progress even though the item carries a real duration. The stream is
 * still not seekable; this only restores the position the session reports.
 */
@OptIn(UnstableApi::class)
internal class KnownDurationPlayer(
    player: Player,
) : ForwardingPlayer(player) {
    override fun isCurrentMediaItemLive(): Boolean = if (declaredDurationMs() != null) false else super.isCurrentMediaItemLive()

    private fun declaredDurationMs(): Long? =
        currentMediaItem
            ?.mediaMetadata
            ?.durationMs
            ?.takeIf { it > 0 }
}
