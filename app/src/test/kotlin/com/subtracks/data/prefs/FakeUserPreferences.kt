package com.subtracks.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun fakeUserPreferences(): UserPreferences =
    UserPreferences(
        object : DataStore<Preferences> {
            private val state = MutableStateFlow(emptyPreferences())

            override val data: Flow<Preferences> = state

            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                transform(state.value).also { state.value = it }
        },
    )
