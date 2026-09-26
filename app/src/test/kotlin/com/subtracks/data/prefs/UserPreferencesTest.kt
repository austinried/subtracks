package com.subtracks.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.net.NetworkMode
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

            prefs.setListQuery(LibraryListTab.Albums, ListQuery("Added", descending = true, starred = StarredFilter.Starred))
            prefs.setListQuery(LibraryListTab.Artists, ListQuery("AlbumCount", descending = true))

            assertEquals(
                ListQuery("Added", descending = true, starred = StarredFilter.Starred),
                prefs.listQuery(LibraryListTab.Albums).first(),
            )
            assertEquals(
                ListQuery("AlbumCount", descending = true),
                prefs.listQuery(LibraryListTab.Artists).first(),
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
            assertEquals(StreamQuality(), prefs.streamQuality(NetworkMode.Mobile).first())

            prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(320, null))
            prefs.setStreamQuality(NetworkMode.Mobile, StreamQuality(96, "opus"))

            assertEquals(StreamQuality(320, null), prefs.streamQuality(NetworkMode.Wifi).first())
            assertEquals(StreamQuality(96, "opus"), prefs.streamQuality(NetworkMode.Mobile).first())

            file.delete()
        }

    @Test
    fun streamQualityFallsBackToTheLegacySingleValue() =
        runTest {
            val file = File.createTempFile("user-prefs", ".preferences_pb").apply { delete() }
            val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
            store.edit { prefs ->
                prefs[intPreferencesKey("max_bitrate")] = 192
                prefs[stringPreferencesKey("stream_format")] = "mp3"
            }
            val prefs = UserPreferences(store)

            assertEquals(StreamQuality(192, "mp3"), prefs.streamQuality(NetworkMode.Wifi).first())
            assertEquals(StreamQuality(192, "mp3"), prefs.streamQuality(NetworkMode.Mobile).first())

            prefs.setStreamQuality(NetworkMode.Mobile, StreamQuality(64, null))

            assertEquals(StreamQuality(192, "mp3"), prefs.streamQuality(NetworkMode.Wifi).first())
            assertEquals(StreamQuality(64, null), prefs.streamQuality(NetworkMode.Mobile).first())

            file.delete()
        }
}
