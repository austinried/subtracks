package com.subtracks.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreRowsTest {
    private fun weight(genre: String) = genre.length + 6

    @Test
    fun everyGenreIsPlacedExactlyOnce() {
        val genres = listOf("Electronic", "Rock", "Pop", "Jazz", "Classical", "Ambient")

        val rows = brickRows(genres, 3)

        assertEquals(3, rows.size)
        assertEquals(genres.sorted(), rows.flatten().sorted())
    }

    @Test
    fun rowsBalanceByWidthNotCount() {
        val genres =
            listOf(
                "VeryLongGenreName1",
                "A",
                "B",
                "VeryLongGenreName2",
                "C",
                "D",
                "VeryLongGenreName3",
                "E",
                "F",
            )

        val rows = brickRows(genres, 3)

        val widths = rows.map { row -> row.sumOf(::weight) }
        val largestChip = genres.maxOf(::weight)
        assertTrue(widths.max() - widths.min() <= largestChip)
    }
}
