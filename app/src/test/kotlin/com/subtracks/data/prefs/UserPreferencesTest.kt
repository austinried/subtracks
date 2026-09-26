package com.subtracks.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
