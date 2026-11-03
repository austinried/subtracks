package com.subtracks.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumCreditsTest {
    @Test
    fun everyVariantUsesAllFourCreditPaths() {
        val variants =
            listOf(
                CREDITED_ALBUM_ROWS,
                DOWNLOADED_ALBUM_ROWS,
                DOWNLOADED_ALBUM_DETAILS_FOR_ARTIST,
                DOWNLOADED_COVER_ALBUM_ROWS,
                ARTIST_COVER_CREDITS,
                ARTIST_COVER_CREDITS_FOR_ARTIST,
                ALBUMS_FOR_ARTIST_DOWNLOADED,
            )
        variants.forEach { sql ->
            assertEquals(sql, 1, sql.occurrences("FROM albums"))
            assertEquals(sql, 1, sql.occurrences("FROM album_artists"))
            assertEquals(sql, 1, sql.occurrences("FROM songs JOIN albums"))
            assertEquals(sql, 1, sql.occurrences("FROM song_artists"))
        }
    }

    @Test
    fun variantsKeepTheirOwnFiltersAndDuplicateSemantics() {
        assertFalse(CREDITED_ALBUM_ROWS.contains("coverArt IS NOT NULL"))
        assertFalse(CREDITED_ALBUM_ROWS.contains("song_downloads"))
        assertTrue(CREDITED_ALBUM_ROWS.contains(" UNION ALL "))
        assertEquals(4, ARTIST_COVER_CREDITS.occurrences("coverArt IS NOT NULL"))
        assertEquals(4, ARTIST_COVER_CREDITS_FOR_ARTIST.occurrences("= :artistId"))
        assertEquals(4, DOWNLOADED_ALBUM_ROWS.occurrences("sd.status = 'Completed'"))
        assertEquals(4, DOWNLOADED_ALBUM_DETAILS_FOR_ARTIST.occurrences("= :artistId"))
        assertEquals(4, DOWNLOADED_COVER_ALBUM_ROWS.occurrences("coverArt IS NOT NULL"))
        assertTrue(DOWNLOADED_ALBUM_ROWS.contains(" UNION "))
        assertFalse(DOWNLOADED_ALBUM_ROWS.contains(" UNION ALL "))
        assertFalse(DOWNLOADED_ALBUM_ROWS.contains("creditType"))
    }

    @Test
    fun consumersEmbedTheSharedVariants() {
        assertTrue(ALBUMS_FOR_ARTIST.contains(CREDITED_ALBUM_ROWS))
        assertTrue(ALBUM_CREDIT_ALBUM_IDS.contains(CREDITED_ALBUM_ROWS))
        assertTrue(ARTIST_AGGREGATES_SQL.contains(CREDITED_ALBUM_ROWS))
        assertTrue(DOWNLOADED_ARTIST_ALBUMS.contains(DOWNLOADED_ALBUM_ROWS))
        assertTrue(ALBUMS_FOR_ARTIST_DOWNLOADED.contains(DOWNLOADED_ALBUM_DETAILS_FOR_ARTIST))
        assertTrue(ARTIST_COVER_ARTS_SQL.contains(ARTIST_COVER_CREDITS_FOR_ARTIST))
        assertTrue(ARTIST_ART_UPDATE_SQL.contains(ARTIST_COVER_CREDITS))
        assertTrue(ARTIST_ART_UPDATE_SQL.contains(DOWNLOADED_COVER_ALBUM_ROWS))
        assertTrue(DOWNLOADED_ARTIST_ART_UPDATE_SQL.contains(DOWNLOADED_COVER_ALBUM_ROWS))
    }

    private fun String.occurrences(value: String): Int = split(value).size - 1
}
