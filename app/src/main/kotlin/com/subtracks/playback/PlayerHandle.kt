package com.subtracks.playback

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

    fun setWindow(
        items: List<QueueItem>,
        startIndex: Int,
        startPositionMs: Long = 0,
    )

    fun addFirst(item: QueueItem)

    fun addLast(item: QueueItem)

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

    fun seekToIndex(index: Int)

    fun seekTo(positionMs: Long)

    fun addListener(listener: Listener)

    interface Listener {
        fun onTransition()

        fun onEvents()

        fun onError(message: String) {
        }
    }
}
