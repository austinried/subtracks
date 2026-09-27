package com.subtracks.data.repo

/**
 * A deterministic, seekable permutation of `[0, size)`: `toFlat(seed, size, s)` is the track played at
 * shuffled position `s` and `toSequence` is its inverse. Nothing per track is stored; the order is derived
 * from the seed and the queue length.
 *
 * The permutation is a balanced four-round Feistel network over the smallest power of two domain that
 * covers `size`, with `[size, domain)` skipped by cycle walking. A Feistel round is always a bijection,
 * so the only requirement is that the two halves have equal width; cycle walking keeps it a bijection on
 * `[0, size)`. The round function is a SplitMix64 mix of the seed, the half value and the round index.
 */
object Shuffle {
    private const val ROUNDS = 4
    private const val MAX_SIZE = 1L shl 40

    private val GOLDEN = 0x9E3779B97F4A7C15uL.toLong()
    private val MIX_A = 0xBF58476D1CE4E5B9uL.toLong()
    private val MIX_B = 0x94D049BB133111EBuL.toLong()

    fun toFlat(
        seed: Long,
        size: Long,
        sequence: Long,
    ): Long {
        require(size <= MAX_SIZE) { "queue of $size tracks is too large to shuffle" }
        if (size <= 1L) return 0L
        if (sequence !in 0 until size) return sequence
        var value = encrypt(seed, sequence, halfBits(size))
        while (value >= size) value = encrypt(seed, value, halfBits(size))
        return value
    }

    fun toSequence(
        seed: Long,
        size: Long,
        flat: Long,
    ): Long {
        require(size <= MAX_SIZE) { "queue of $size tracks is too large to shuffle" }
        if (size <= 1L) return 0L
        if (flat !in 0 until size) return flat
        var value = decrypt(seed, flat, halfBits(size))
        while (value >= size) value = decrypt(seed, value, halfBits(size))
        return value
    }

    private fun halfBits(size: Long): Int {
        if (size <= 1L) return 1
        val bits = 64 - java.lang.Long.numberOfLeadingZeros(size - 1)
        return (bits + 1) / 2
    }

    private fun encrypt(
        seed: Long,
        value: Long,
        halfBits: Int,
    ): Long {
        val mask = (1L shl halfBits) - 1
        var left = value ushr halfBits
        var right = value and mask
        for (round in 0 until ROUNDS) {
            val nextLeft = right
            val nextRight = left xor (mix(seed, right, round) and mask)
            left = nextLeft
            right = nextRight
        }
        return (left shl halfBits) or right
    }

    private fun decrypt(
        seed: Long,
        value: Long,
        halfBits: Int,
    ): Long {
        val mask = (1L shl halfBits) - 1
        var left = value ushr halfBits
        var right = value and mask
        for (round in ROUNDS - 1 downTo 0) {
            val previousLeft = right xor (mix(seed, left, round) and mask)
            val previousRight = left
            left = previousLeft
            right = previousRight
        }
        return (left shl halfBits) or right
    }

    private fun mix(
        seed: Long,
        value: Long,
        round: Int,
    ): Long {
        var z = seed + value * GOLDEN + round * MIX_A
        z = (z xor (z ushr 30)) * MIX_A
        z = (z xor (z ushr 27)) * MIX_B
        return z xor (z ushr 31)
    }
}
