package org.sammomanyi.mediaccess.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            println("🔴 ThemeRepository error: ${exception.message}")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false
        }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}