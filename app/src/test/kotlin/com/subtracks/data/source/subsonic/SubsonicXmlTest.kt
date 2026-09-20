package com.subtracks.data.source.subsonic

import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Document

class SubsonicXmlTest {
    private fun parse(xml: String): Document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(xml.byteInputStream(Charsets.UTF_8))

    @Test
    fun mapsArtists() {
        val document = parse(
            """
            <subsonic-response status="ok">
              <artists>
                <artist id="ar1" name="Radiohead" albumCount="9" starred="2020-01-02T03:04:05.000Z"/>
                <artist id="ar2" name="Portishead" albumCount="3"/>
              </artists>
            </subsonic-response>
            """.trimIndent(),
        )

        val artists = SubsonicXml.artists(7, document)

        assertEquals(2, artists.size)
        assertEquals("Radiohead", artists[0].name)
        assertEquals(7L, artists[0].sourceId)
        assertEquals(9L, artists[0].albumCount)
        assertEquals(1577934245L, artists[0].starred)
        assertEquals(null, artists[1].starred)
    }

    @Test
    fun mapsAlbumList() {
        val document = parse(
            """
            <subsonic-response status="ok">
              <albumList2>
                <album id="al1" name="OK Computer" artist="Radiohead" artistId="ar1" created="1997-05-21T00:00:00.000Z" songCount="12" coverArt="al1"/>
                <album id="al2" name="Kid A" artist="Radiohead" artistId="ar1" created="2000-10-02T00:00:00.000Z" songCount="10" coverArt="al2"/>
              </albumList2>
            </subsonic-response>
            """.trimIndent(),
        )

        val albums = SubsonicXml.albums(1, document)

        assertEquals(2, albums.size)
        assertEquals("OK Computer", albums[0].name)
        assertEquals("Kid A", albums[1].name)
        assertEquals("ar1", albums[0].artistId)
        assertEquals("Radiohead", albums[0].albumArtist)
        assertEquals(12L, albums[0].songCount)
    }

    @Test
    fun parsesTimestampsWithFractionsAndOffsets() {
        assertEquals(1789906657L, IsoDate.parse("2026-09-20T12:17:37.370827413Z"))
        assertEquals(1789906657L, IsoDate.parse("2026-09-20T12:17:37.370Z"))
        assertEquals(1789906657L, IsoDate.parse("2026-09-20T12:17:37Z"))
        assertEquals(1789906657L, IsoDate.parse("2026-09-20T12:17:37"))
        assertEquals(1789906657L, IsoDate.parse("2026-09-20T14:17:37.370827413+02:00"))
        assertEquals(null, IsoDate.parse("not a date"))
    }

    @Test
    fun mapsSongsAndPlaylistEntries() {
        val document = parse(
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="sg2" title="Idioteque" artist="Radiohead" album="Kid A" albumId="al2" artistId="ar1" duration="300" track="8" discNumber="1"/>
              </searchResult3>
              <playlist id="pl1" name="Favourites" songCount="1" created="2021-02-03T04:05:06.000Z">
                <entry id="sg1" title="Everything In Its Right Place" artist="Radiohead" album="Kid A" albumId="al2" artistId="ar1" duration="251" track="1" discNumber="1"/>
              </playlist>
            </subsonic-response>
            """.trimIndent(),
        )

        val songs = SubsonicXml.songs(1, document)
        val entries = SubsonicXml.playlistSongs(1, "pl1", document)

        assertEquals(1, songs.size)
        assertEquals("Idioteque", songs[0].title)
        assertEquals(300L, songs[0].duration)
        assertEquals(0L, entries[0].position)
        assertEquals("sg1", entries[0].songId)
        assertEquals(251L, SubsonicXml.song(1, document.getElementsByTagName("entry").item(0) as org.w3c.dom.Element).duration)
    }
}
