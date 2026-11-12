package com.subtracks.data.repo

import com.subtracks.data.model.QueueEntry

suspend fun QueueRepository.replace(
    entries: List<QueueEntry>,
    shuffleSeed: Long? = null,
    upNext: List<QueueEntry> = emptyList(),
    upNextAnchor: Long = 0,
) = withMutationLock {
    replace(entries, shuffleSeed, upNext, upNextAnchor)
}

suspend fun QueueRepository.setShuffle(seed: Long?) =
    withMutationLock {
        setShuffle(seed)
    }

suspend fun QueueRepository.addUpNext(
    currentPosition: Long,
    entry: QueueEntry,
    playNext: Boolean,
): Long = withMutationLock { addUpNext(currentPosition, entry, playNext) }

suspend fun QueueRepository.reanchorFor(
    snapshot: QueueSnapshot,
    target: Long,
): Reanchored = withMutationLock { reanchorFor(snapshot, target) }

suspend fun QueueRepository.clearUpNext() =
    withMutationLock {
        clearUpNext()
    }

suspend fun QueueRepository.removeUpNextAt(
    snapshot: QueueSnapshot,
    index: Long,
): Boolean = withMutationLock { removeUpNextAt(snapshot, index) }

suspend fun QueueRepository.removeAt(
    snapshot: QueueSnapshot,
    position: Long,
): Boolean = withMutationLock { removeAt(snapshot, position) }

suspend fun QueueRepository.move(
    snapshot: QueueSnapshot,
    from: Long,
    to: Long,
): Boolean = withMutationLock { move(snapshot, from, to) }
