package com.subtracks.ui.home

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.R
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeListInfoTest {
    private val now = 1_700_000_000_000L
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun relativeTimeUsesFriendlyBuckets() {
        assertNull(relativeTimeLabel(0, now, resources))
        assertEquals("Just now", relativeTimeLabel(now / 1000 - 10, now, resources))
        assertEquals("Today", relativeTimeLabel((now - 3 * 3_600_000L) / 1000L, now, resources))
        assertEquals("Yesterday", relativeTimeLabel(secondsAgo(1), now, resources))
        assertEquals("3 days ago", relativeTimeLabel(secondsAgo(3), now, resources))
        assertEquals("Last week", relativeTimeLabel(secondsAgo(10), now, resources))
        assertEquals("3 weeks ago", relativeTimeLabel(secondsAgo(21), now, resources))
        assertEquals("Last month", relativeTimeLabel(secondsAgo(45), now, resources))
        assertEquals("3 months ago", relativeTimeLabel(secondsAgo(100), now, resources))
        assertEquals("Last year", relativeTimeLabel(secondsAgo(400), now, resources))
        assertEquals("2 years ago", relativeTimeLabel(secondsAgo(800), now, resources))
    }

    @Test
    fun albumInfoMatchesTheSection() {
        val album = album(created = secondsAgo(1), played = secondsAgo(3), playCount = 5, year = 1999)

        assertEquals("Yesterday", albumListInfo(request(HomeSection.RecentlyAddedAlbums), album, now, resources))
        assertEquals("3 days ago", albumListInfo(request(HomeSection.RecentlyPlayedAlbums), album, now, resources))
        assertEquals("5 plays", albumListInfo(request(HomeSection.MostPlayedAlbums), album, now, resources))
        assertEquals("3 days ago", albumListInfo(request(HomeSection.Rediscover), album, now, resources))
    }

    @Test
    fun albumInfoIsNullWhenTheSectionHasNothingToShow() {
        val album = album(created = 0, played = null, playCount = 0, year = 0)

        assertNull(albumListInfo(request(HomeSection.RecentlyAddedAlbums), album, now, resources))
        assertNull(albumListInfo(request(HomeSection.RecentlyPlayedAlbums), album, now, resources))
        assertNull(albumListInfo(request(HomeSection.MostPlayedAlbums), album, now, resources))
    }

    @Test
    fun aDecadeListShowsTheYear() {
        val album = album(created = 0, played = null, playCount = 0, year = 1999)

        assertEquals(
            "1999",
            albumListInfo(HomeListRequest(title = resources.getString(R.string.home_decade, 1990L), decade = 1990), album, now, resources),
        )
    }

    @Test
    fun artistInfoMatchesTheSection() {
        val artist = artist(played = secondsAgo(3), playCount = 7)

        assertEquals("3 days ago", artistListInfo(request(HomeSection.RecentlyPlayedArtists), artist, now, resources))
        assertEquals("7 plays", artistListInfo(request(HomeSection.MostPlayedArtists), artist, now, resources))
        assertNull(artistListInfo(request(HomeSection.MostPlayedArtists), artist.copy(playCount = 0, played = null), now, resources))
    }

    private fun secondsAgo(days: Long): Long = (now - days * 86_400_000L) / 1000L

    private fun request(section: HomeSection) = HomeListRequest(title = resources.getString(section.title), section = section)

    private fun album(
        created: Long,
        played: Long?,
        playCount: Long,
        year: Long,
    ) = Album(
        sourceId = 1,
        id = "a",
        artistId = "ar",
        name = "Album",
        albumArtist = "Artist",
        created = created,
        coverArt = null,
        genre = null,
        year = year,
        starred = null,
        songCount = 1,
        playCount = playCount,
        played = played,
    )

    private fun artist(
        played: Long?,
        playCount: Long,
    ) = Artist(
        sourceId = 1,
        id = "ar",
        name = "Artist",
        albumCount = 1,
        starred = null,
        coverArt = null,
        playCount = playCount,
        played = played,
    )
}
