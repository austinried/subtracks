package com.subtracks.data.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ShuffleTest {
    private val sizes =
        listOf(
            1L,
            2L,
            3L,
            4L,
            5L,
            7L,
            8L,
            9L,
            15L,
            16L,
            17L,
            31L,
            32L,
            33L,
            63L,
            64L,
            65L,
            100L,
            127L,
            128L,
            129L,
            255L,
            256L,
            257L,
            999L,
            1000L,
            1023L,
            1024L,
            1025L,
            4096L,
            65535L,
            65536L,
            65537L,
            100_000L,
            999_983L,
        )

    private val seeds = listOf(0L, 1L, -1L, 42L, Long.MIN_VALUE, Long.MAX_VALUE, 0x5DEECE66DL)

    @Test
    fun mapsEverySizeAsABijection() {
        for (size in sizes) {
            for (seed in seeds) {
                val mapped = (0 until size).map { Shuffle.toFlat(seed, size, it) }
                assertEquals("size=$size seed=$seed has duplicates", mapped.distinct().size, size.toInt())
                assertTrue("size=$size seed=$seed out of range", mapped.all { it in 0 until size })
                for (sequence in 0 until size) {
                    assertEquals(
                        "size=$size seed=$seed sequence=$sequence did not round trip",
                        sequence,
                        Shuffle.toSequence(seed, size, Shuffle.toFlat(seed, size, sequence)),
                    )
                }
            }
        }
    }

    @Test
    fun mapsLargeSizesAsABijectionOnSamples() {
        for (size in listOf(1_000_003L, 16_777_216L, 16_777_217L, 1_000_000_000L)) {
            for (seed in seeds) {
                val samples = (0 until 5000).map { size / 5000 * it + it % 97 }
                val mapped = samples.map { Shuffle.toFlat(seed, size, it) }
                assertEquals(mapped.distinct().size, mapped.size)
                assertTrue(mapped.all { it in 0 until size })
                samples.forEach { sequence ->
                    assertEquals(sequence, Shuffle.toSequence(seed, size, Shuffle.toFlat(seed, size, sequence)))
                }
            }
        }
    }

    @Test
    fun spreadsTheFirstPositionAcrossSeeds() {
        val size = 1000L
        val buckets = LongArray(10)
        val draws = 2000
        for (i in 0 until draws) {
            val flat = Shuffle.toFlat(i.toLong() * 2654435761L + 17L, size, 0L)
            buckets[(flat * buckets.size / size).toInt()]++
        }
        val expected = draws / buckets.size
        buckets.forEach { count ->
            assertTrue("bucket $count is far from $expected", count in expected / 2..expected * 2)
        }
    }

    @Test
    fun doesNotLeaveMostTracksInPlace() {
        val size = 1000L
        for (seed in seeds) {
            val moved = (0 until size).count { Shuffle.toFlat(seed, size, it) != it }
            assertTrue("seed=$seed left too many tracks in place", moved > size * 9 / 10)
        }
    }

    @Test
    fun handlesDegenerateAndOutOfRangeInputs() {
        assertEquals(0L, Shuffle.toFlat(1L, 0L, 5L))
        assertEquals(0L, Shuffle.toSequence(1L, 0L, 5L))
        assertEquals(0L, Shuffle.toFlat(1L, 1L, 0L))
        assertEquals(10L, Shuffle.toFlat(1L, 10L, 10L))
        assertEquals(10L, Shuffle.toSequence(1L, 10L, 10L))
    }

    @Test
    fun rejectsSizesBeyondThePermutationDomain() {
        assertThrows(IllegalArgumentException::class.java) {
            Shuffle.toFlat(1L, (1L shl 40) + 1L, 0L)
        }
    }

    @Test
    fun consecutivePositionsAreNotASingleStride() {
        val size = 1000L
        val order = (0 until size).map { Shuffle.toFlat(7L, size, it) }
        val strides = order.zipWithNext { a, b -> ((b - a) % size + size) % size }.toSet()
        assertTrue("the order looks like one fixed stride", strides.size > size / 2)
    }
}
