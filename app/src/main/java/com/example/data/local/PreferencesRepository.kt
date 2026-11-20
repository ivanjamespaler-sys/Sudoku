package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.UserPreferences
import com.example.domain.model.WoodTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timber_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val WOOD_THEME = stringPreferencesKey("wood_theme")
        val ZEN_MODE = booleanPreferencesKey("zen_mode")
        val AUTO_CLEAN_NOTES = booleanPreferencesKey("auto_clean_notes")
        val HIGHLIGHT_SAME = booleanPreferencesKey("highlight_same")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeName = prefs[Keys.WOOD_THEME] ?: WoodTheme.HONEY_OAK.name
        val theme = WoodTheme.entries.firstOrNull { it.name == themeName } ?: WoodTheme.HONEY_OAK
        UserPreferences(
            woodTheme = theme,
            zenMode = prefs[Keys.ZEN_MODE] ?: false,
            autoCleanNotes = prefs[Keys.AUTO_CLEAN_NOTES] ?: true,
            highlightSameDigits = prefs[Keys.HIGHLIGHT_SAME] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true
        )
    }

    suspend fun setWoodTheme(theme: WoodTheme) {
        context.dataStore.edit { it[Keys.WOOD_THEME] = theme.name }
    }

    suspend fun setZenMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ZEN_MODE] = enabled }
    }

    suspend fun setAutoCleanNotes(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_CLEAN_NOTES] = enabled }
    }

    suspend fun setHighlightSameDigits(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGHLIGHT_SAME] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }
}
