package com.subtracks.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.source.DEFAULT_FETCH_CONCURRENCY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserPreferencesTest {
    @Test
    fun listQueryRoundTripsPerTab() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(ListQuery("Name"), prefs.listQuery(LibraryListTab.Albums).first())
            assertEquals(ListQuery("Name"), prefs.listQuery(LibraryListTab.Artists).first())

            prefs.setListQuery(
                LibraryListTab.Albums,
                ListQuery("Added", descending = true, starred = StarredFilter.Starred, downloaded = true),
            )
            prefs.setListQuery(LibraryListTab.Artists, ListQuery("AlbumCount", descending = true))

            assertEquals(
                ListQuery("Added", descending = true, starred = StarredFilter.Starred, downloaded = true),
                prefs.listQuery(LibraryListTab.Albums).first(),
            )
            assertEquals(
                ListQuery("AlbumCount", descending = true),
                prefs.listQuery(LibraryListTab.Artists).first(),
            )

            file.delete()
        }

    @Test
    fun aListQueryStoredBeforeTheDownloadedFilterStillDecodes() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            store.edit { it[stringPreferencesKey("list_query_albums")] = "Added|1|1" }

            assertEquals(
                ListQuery("Added", descending = true, starred = StarredFilter.Starred),
                prefs.listQuery(LibraryListTab.Albums).first(),
            )

            file.delete()
        }

    @Test
    fun streamQualityRoundTripsPerNetworkMode() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(StreamQuality(), prefs.streamQuality(NetworkMode.Wifi).first())
            assertEquals(StreamQuality(192, "mp3"), prefs.streamQuality(NetworkMode.Mobile).first())

            prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(320, null))
            prefs.setStreamQuality(NetworkMode.Mobile, StreamQuality(96, "opus"))

            assertEquals(StreamQuality(320, null), prefs.streamQuality(NetworkMode.Wifi).first())
            assertEquals(StreamQuality(96, "opus"), prefs.streamQuality(NetworkMode.Mobile).first())

            file.delete()
        }

    @Test
    fun downloadQualityAndMeteredRoundTrip() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(StreamQuality(), prefs.downloadQuality().first())
            assertEquals(false, prefs.downloadOverMetered().first())

            prefs.setDownloadQuality(StreamQuality(192, "opus"))
            prefs.setDownloadOverMetered(true)

            assertEquals(StreamQuality(192, "opus"), prefs.downloadQuality().first())
            assertEquals(true, prefs.downloadOverMetered().first())

            file.delete()
        }

    @Test
    fun syncConcurrencyDefaultsAndClampsToAtLeastOne() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(DEFAULT_FETCH_CONCURRENCY, prefs.syncConcurrency().first())

            prefs.setSyncConcurrency(8)
            assertEquals(8, prefs.syncConcurrency().first())

            prefs.setSyncConcurrency(0)
            assertEquals(1, prefs.syncConcurrency().first())

            file.delete()
        }

    @Test
    fun scrobblingDefaultsOnAndRoundTrips() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(true, prefs.scrobbling().first())

            prefs.setScrobbling(false)
            assertEquals(false, prefs.scrobbling().first())

            file.delete()
        }

    @Test
    fun lastSeedRoundTripsWithAndWithoutASecondary() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            val prefs = UserPreferences(store)

            assertEquals(null, prefs.lastSeed())

            prefs.setLastSeed(ArtworkSeedValue("art:1", 0xFF112233.toInt(), 0xFF445566.toInt()))
            assertEquals(
                ArtworkSeedValue("art:1", 0xFF112233.toInt(), 0xFF445566.toInt()),
                prefs.lastSeed(),
            )

            prefs.setLastSeed(ArtworkSeedValue("art:2", 0xFF778899.toInt(), null))
            assertEquals(ArtworkSeedValue("art:2", 0xFF778899.toInt(), null), prefs.lastSeed())

            file.delete()
        }
}
