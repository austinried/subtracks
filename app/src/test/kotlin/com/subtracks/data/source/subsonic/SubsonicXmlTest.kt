package com.subtracks.data.source.subsonic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.xml.sax.Attributes
import java.io.FilterInputStream
import java.io.InputStream

class SubsonicXmlTest {
    private fun <T> parse(
        xml: String,
        tag: String,
        create: (Attributes) -> T,
        onChild: (T, String, Attributes) -> Unit = { _, _, _ -> },
    ): List<T> {
        val result = mutableListOf<T>()
        SubsonicXml.readEntities(
            input = xml.byteInputStream(Charsets.UTF_8),
            entityTag = tag,
            create = create,
            onChild = onChild,
            onEnd = { result += it },
        )
        return result
    }

    private fun albums(xml: String) =
        parse(
            xml,
            tag = "album",
            create = { SubsonicXml.albumDraft(1, it) },
            onChild = { draft, name, attrs -> if (name == "discTitles") draft.addDiscTitle(attrs) },
        ).map { it.toAlbum() }

    @Test
    fun mapsArtists() {
        val artists =
            parse(
                """
                <subsonic-response status="ok">
                  <artists>
                    <artist id="ar1" name="Radiohead" albumCount="9" starred="2020-01-02T03:04:05.000Z"/>
                    <artist id="ar2" name="Portishead" albumCount="3"/>
                  </artists>
                </subsonic-response>
                """.trimIndent(),
                tag = "artist",
                create = { SubsonicXml.artist(7, it) },
            )

        assertEquals(2, artists.size)
        assertEquals("Radiohead", artists[0].name)
        assertEquals(7L, artists[0].sourceId)
        assertEquals(9L, artists[0].albumCount)
        assertEquals(1577934245L, artists[0].starred)
        assertEquals(null, artists[1].starred)
    }

    @Test
    fun mapsAlbumList() {
        val albums =
            albums(
                """
                <subsonic-response status="ok">
                  <albumList2>
                    <album id="al1" name="OK Computer" artist="Radiohead" artistId="ar1" created="1997-05-21T00:00:00.000Z" songCount="12" coverArt="al1"/>
                    <album id="al2" name="Kid A" artist="Radiohead" artistId="ar1" created="2000-10-02T00:00:00.000Z" songCount="10" coverArt="al2"/>
                  </albumList2>
                </subsonic-response>
                """.trimIndent(),
            )

        assertEquals(2, albums.size)
        assertEquals("OK Computer", albums[0].name)
        assertEquals("Kid A", albums[1].name)
        assertEquals("ar1", albums[0].artistId)
        assertEquals("Radiohead", albums[0].albumArtist)
        assertEquals(12L, albums[0].songCount)
    }

    @Test
    fun mapsAlbumDiscTitles() {
        val albums =
            albums(
                """
                <subsonic-response status="ok">
                  <albumList2>
                    <album id="al1" name="The Wall" artist="Pink Floyd" artistId="ar1" songCount="26">
                      <discTitles disc="1" title="The Calm"/>
                      <discTitles disc="2" title="The Storm"/>
                      <discTitles disc="3" title=""/>
                    </album>
                  </albumList2>
                </subsonic-response>
                """.trimIndent(),
            )

        assertEquals(mapOf(1L to "The Calm", 2L to "The Storm"), albums.single().discTitles)
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
        val xml =
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="sg2" title="Idioteque" artist="Radiohead" album="Kid A" albumId="al2" artistId="ar1" duration="300" track="8" discNumber="1"/>
              </searchResult3>
              <playlist id="pl1" name="Favourites" songCount="1" created="2021-02-03T04:05:06.000Z">
                <entry id="sg1" title="Everything In Its Right Place" artist="Radiohead" album="Kid A" albumId="al2" artistId="ar1" duration="251" track="1" discNumber="1"/>
              </playlist>
            </subsonic-response>
            """.trimIndent()

        val songs = parse(xml, tag = "song", create = { SubsonicXml.song(1, it) })
        var position = 0L
        val entries = parse(xml, tag = "entry", create = { SubsonicXml.playlistSong(1, "pl1", position++, it) })

        assertEquals(1, songs.size)
        assertEquals("Idioteque", songs[0].title)
        assertEquals(300L, songs[0].duration)
        assertEquals(0L, entries[0].position)
        assertEquals("sg1", entries[0].songId)
    }

    @Test
    fun mapsSongPlayData() {
        val xml =
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="s1" title="Played" playCount="12" played="2023-03-26T22:27:46Z"/>
                <song id="s2" title="Never"/>
              </searchResult3>
            </subsonic-response>
            """.trimIndent()

        val songs = parse(xml, tag = "song", create = { SubsonicXml.song(1, it) })

        assertEquals(12L, songs[0].playCount)
        assertEquals(1679869666L, songs[0].played)
        assertEquals(0L, songs[1].playCount)
        assertEquals(null, songs[1].played)
    }

    @Test
    fun mapsSongGenresAndIgnoresEmptyOnes() {
        val xml =
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="s1" title="Multi">
                  <genres name="Rock"/>
                  <genres name="Electronic"/>
                  <genres name=""/>
                  <genres name="   "/>
                </song>
              </searchResult3>
            </subsonic-response>
            """.trimIndent()

        val songs =
            parse(
                xml,
                tag = "song",
                create = { SubsonicXml.songDraft(1, it) },
                onChild = { draft, name, attrs -> if (name == "genres") draft.addGenre(attrs) },
            ).map { it.toSong() }

        assertEquals(listOf("Rock", "Electronic"), songs.single().genres)
    }

    @Test
    fun ignoresSongGenresThatAreNotDirectChildren() {
        val xml =
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="s1" title="Nested">
                  <wrapper><genres name="Rock"/></wrapper>
                </song>
              </searchResult3>
            </subsonic-response>
            """.trimIndent()

        val songs =
            parse(
                xml,
                tag = "song",
                create = { SubsonicXml.songDraft(1, it) },
                onChild = { draft, name, attrs -> if (name == "genres") draft.addGenre(attrs) },
            ).map { it.toSong() }

        assertEquals(emptyList<String>(), songs.single().genres)
    }

    @Test
    fun statusOnlyReadRejectsAnHtmlLoginPage() {
        val failure =
            runCatching {
                SubsonicXml.readStatus("<html><body>Sign in</body></html>".byteInputStream(Charsets.UTF_8))
            }

        assertEquals(true, failure.exceptionOrNull() is SubsonicException)
    }

    @Test
    fun containsEntityStopsAtTheFirstMatch() {
        val xml =
            """
            <subsonic-response status="ok">
              <searchResult3>
                <song id="s1" title="One"/>
                <song id="s2" title="Two"/>
              </searchResult3>
            </subsonic-response>
            """.trimIndent()

        assertEquals(true, SubsonicXml.containsEntity(xml.byteInputStream(Charsets.UTF_8), "song"))
        assertEquals(false, SubsonicXml.containsEntity(xml.byteInputStream(Charsets.UTF_8), "artist"))
    }

    @Test
    fun containsEntityStopsReadingAtTheFirstMatch() {
        val xml =
            buildString {
                append("<subsonic-response status=\"ok\"><searchResult3>")
                repeat(5_000) { append("<song id=\"s$it\" title=\"Song $it\"/>") }
                append("</searchResult3></subsonic-response>")
            }
        val counting = CountingInputStream(xml.byteInputStream(Charsets.UTF_8))

        assertEquals(true, SubsonicXml.containsEntity(counting, "song"))

        assertTrue("read ${counting.bytesRead} of ${xml.length}", counting.bytesRead < 32_000)
    }

    @Test
    fun rejectsATruncatedResponse() {
        val failure =
            runCatching {
                SubsonicXml.readStatus("<subsonic-response status=\"ok\"><artists>".byteInputStream(Charsets.UTF_8))
            }

        assertEquals(true, failure.exceptionOrNull() is SubsonicException)
    }

    @Test
    fun failedStatusWithoutAnErrorStillThrows() {
        val failure =
            runCatching {
                SubsonicXml.readStatus(
                    "<subsonic-response status=\"failed\"></subsonic-response>".byteInputStream(Charsets.UTF_8),
                )
            }

        assertEquals(true, failure.exceptionOrNull() is SubsonicException)
    }

    @Test
    fun errorElementWithoutStatusThrowsWithItsCode() {
        val error =
            runCatching {
                SubsonicXml.readStatus(
                    "<subsonic-response><error code=\"40\" message=\"Wrong username\"/></subsonic-response>"
                        .byteInputStream(Charsets.UTF_8),
                )
            }.exceptionOrNull() as? SubsonicException

        assertEquals(40, error?.code)
        assertEquals("Wrong username", error?.message)
    }

    @Test
    fun nestedSameNameElementsEmitOnce() {
        val albums =
            albums(
                """
                <subsonic-response status="ok">
                  <albumList2>
                    <album id="al1" name="Outer">
                      <album id="al2" name="Inner"/>
                    </album>
                  </albumList2>
                </subsonic-response>
                """.trimIndent(),
            )

        assertEquals(listOf("al1"), albums.map { it.id })
    }

    @Test
    fun ignoresDiscTitlesThatAreNotDirectChildren() {
        val albums =
            albums(
                """
                <subsonic-response status="ok">
                  <albumList2>
                    <album id="al1" name="The Wall">
                      <wrapper><discTitles disc="1" title="The Calm"/></wrapper>
                    </album>
                  </albumList2>
                </subsonic-response>
                """.trimIndent(),
            )

        assertEquals(emptyMap<Long, String>(), albums.single().discTitles)
    }

    @Test
    fun discTitleWithoutADiscNumberIsIgnored() {
        val albums =
            albums(
                """
                <subsonic-response status="ok">
                  <albumList2>
                    <album id="al1" name="The Wall">
                      <discTitles title="No number"/>
                      <discTitles disc="2" title="The Storm"/>
                    </album>
                  </albumList2>
                </subsonic-response>
                """.trimIndent(),
            )

        assertEquals(mapOf(2L to "The Storm"), albums.single().discTitles)
    }

    @Test
    fun rejectsExternalEntities() {
        val xml =
            """
            <!DOCTYPE subsonic-response [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <subsonic-response status="ok"><artists><artist id="&xxe;" name="x"/></artists></subsonic-response>
            """.trimIndent()
        val failure =
            runCatching {
                SubsonicXml.readStatus(xml.byteInputStream(Charsets.UTF_8))
            }

        assertEquals(true, failure.exceptionOrNull() is SubsonicException)
    }

    private class CountingInputStream(
        input: InputStream,
    ) : FilterInputStream(input) {
        var bytesRead = 0
            private set

        override fun read(): Int =
            super.read().also {
                if (it >= 0) bytesRead++
            }

        override fun read(
            b: ByteArray,
            off: Int,
            len: Int,
        ): Int =
            super.read(b, off, len).also {
                if (it > 0) bytesRead += it
            }
    }
}
