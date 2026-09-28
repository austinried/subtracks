package com.subtracks.playback

import com.subtracks.data.model.AudioEncoding

interface PlayerHandle {
    val itemCount: Int
    val currentIndex: Int
    val isIdle: Boolean
    val isEnded: Boolean
    val isBuffering: Boolean
    val isPlaying: Boolean
    val playWhenReady: Boolean
    val durationMs: Long
    val currentPositionMs: Long
    val currentItem: QueueItem?

    /** The encoding of the audio now being rendered, when the player knows it. */
    val audioEncoding: AudioEncoding? get() = null

    fun setWindow(
        items: List<QueueItem>,
        startIndex: Int,
        startPositionMs: Long = 0,
    )

    fun addFirst(item: QueueItem)

    fun addLast(item: QueueItem)

    fun insertAt(
        index: Int,
        item: QueueItem,
    )

    fun removeFirst()

    fun removeLast()

    fun removeAt(index: Int)

    fun move(
        from: Int,
        to: Int,
    )

    fun prepare()

    fun play()

    fun pause()

    fun stop()

    fun clear()

    /** The resolved URI of each item in the current window, in window order. */
    fun itemUris(): List<String?> = emptyList()

    /** Rebuilds one window item in place, without re-preparing the playing one. */
    fun replaceItem(
        index: Int,
        item: QueueItem,
    ) = Unit

    fun seekToIndex(index: Int)

    fun seekTo(positionMs: Long)

    fun setRepeatOne(enabled: Boolean)

    fun addListener(listener: Listener)

    interface Listener {
        fun onTransition()

        fun onEvents()

        fun onError(message: String) {
        }
    }
}
