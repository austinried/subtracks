package com.subtracks.playback

import java.util.concurrent.CopyOnWriteArrayList

class FakePlayerConnection(
    private val handle: FakePlayerHandle,
) : PlayerConnection {
    override fun connect(
        onConnected: (PlayerHandle) -> Unit,
        onDisconnected: () -> Unit,
    ) = onConnected(handle)
}

class FakePlayerHandle : PlayerHandle {
    val items = CopyOnWriteArrayList<QueueItem>()
    val operations = CopyOnWriteArrayList<String>()

    @Volatile
    var duration = 0L

    @Volatile
    var positionMs = 0L

    private val listeners = CopyOnWriteArrayList<PlayerHandle.Listener>()

    @Volatile
    private var index = 0

    @Volatile
    private var idle = true

    @Volatile
    private var playing = false

    @Volatile
    private var wantsToPlay = false

    @Volatile
    private var ended = false

    @Volatile
    private var buffering = false

    override val itemCount: Int get() = items.size

    override val currentIndex: Int get() = index

    override val isIdle: Boolean get() = idle

    override val isEnded: Boolean get() = ended

    override val isBuffering: Boolean get() = buffering

    override val isPlaying: Boolean get() = playing

    override val playWhenReady: Boolean get() = wantsToPlay

    override val durationMs: Long get() = duration

    override val currentPositionMs: Long get() = positionMs

    override val currentItem: QueueItem? get() = items.getOrNull(index)

    override fun setWindow(
        items: List<QueueItem>,
        startIndex: Int,
        startPositionMs: Long,
    ) {
        operations += "setWindow(size=${items.size}, start=$startIndex, position=$startPositionMs)"
        this.items.clear()
        this.items += items
        index = startIndex
        positionMs = startPositionMs
        idle = true
        ended = false
        buffering = false
        playing = false
        wantsToPlay = false
        notifyEvents()
    }

    override fun addFirst(item: QueueItem) {
        operations += "addFirst(${item.id})"
        items.add(0, item)
        index++
    }

    override fun addLast(item: QueueItem) {
        operations += "addLast(${item.id})"
        items += item
    }

    override fun insertAt(
        index: Int,
        item: QueueItem,
    ) {
        operations += "insertAt($index, ${item.id})"
        items.add(index, item)
        if (index <= this.index) this.index++
    }

    override fun removeFirst() {
        operations += "removeFirst"
        items.removeAt(0)
        index--
    }

    override fun removeLast() {
        operations += "removeLast"
        items.removeAt(items.size - 1)
    }

    override fun removeAt(index: Int) {
        operations += "removeAt($index)"
        items.removeAt(index)
        this.index =
            when {
                index < this.index -> this.index - 1
                index == this.index -> this.index.coerceAtMost(items.size - 1)
                else -> this.index
            }
        notifyEvents()
    }

    override fun move(
        from: Int,
        to: Int,
    ) {
        operations += "move($from, $to)"
        items.add(to, items.removeAt(from))
        index =
            when {
                index == from -> to
                from < index && to >= index -> index - 1
                from > index && to <= index -> index + 1
                else -> index
            }
        notifyEvents()
    }

    override fun prepare() {
        operations += "prepare"
        idle = false
        notifyEvents()
    }

    override fun play() {
        operations += "play"
        playing = true
        wantsToPlay = true
        notifyEvents()
    }

    override fun pause() {
        operations += "pause"
        playing = false
        wantsToPlay = false
        buffering = false
        notifyEvents()
    }

    override fun stop() {
        operations += "stop"
        playing = false
        wantsToPlay = false
        buffering = false
        idle = true
        ended = false
        notifyEvents()
    }

    override fun clear() {
        operations += "clear"
        items.clear()
        index = 0
        idle = true
        ended = false
        buffering = false
        playing = false
        wantsToPlay = false
        notifyEvents()
    }

    override fun seekToIndex(index: Int) {
        operations += "seekToIndex($index)"
        this.index = index
        ended = false
        notifyEvents()
    }

    override fun seekTo(positionMs: Long) {
        operations += "seekTo($positionMs)"
        ended = false
        notifyEvents()
    }

    override fun setRepeatOne(enabled: Boolean) {
        operations += "setRepeatOne($enabled)"
    }

    override fun addListener(listener: PlayerHandle.Listener) {
        listeners += listener
    }

    fun advanceTo(nextIndex: Int) {
        index = nextIndex
        listeners.forEach { it.onTransition() }
    }

    fun emitEvents() = notifyEvents()

    fun finish() {
        playing = false
        buffering = false
        ended = true
        notifyEvents()
    }

    fun fail(message: String) = listeners.forEach { it.onError(message) }

    fun startBuffering() {
        idle = false
        ended = false
        buffering = true
        playing = false
        wantsToPlay = true
        notifyEvents()
    }

    fun becomeReady() {
        buffering = false
        ended = false
        idle = false
        playing = true
        wantsToPlay = true
        notifyEvents()
    }

    private fun notifyEvents() = listeners.forEach { it.onEvents() }
}
