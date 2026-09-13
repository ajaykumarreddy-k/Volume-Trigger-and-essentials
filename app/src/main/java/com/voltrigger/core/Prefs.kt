package com.voltrigger.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore wrapper for managing control center preferences.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {
    companion object {
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val CONTROL_CENTER_ENABLED = booleanPreferencesKey("control_center_enabled")
        fun getTileEnabledKey(tileClassName: String) = booleanPreferencesKey("tile_enabled_$tileClassName")
    }

    val githubUsernameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[GITHUB_USERNAME] ?: ""
        }

    suspend fun setGithubUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[GITHUB_USERNAME] = username
        }
    }
    
    val controlCenterEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CONTROL_CENTER_ENABLED] ?: false
        }

    suspend fun setControlCenterEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONTROL_CENTER_ENABLED] = enabled
        }
    }

    fun isTileEnabledFlow(tileClassName: String): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Default to true for new tiles on first install
            preferences[getTileEnabledKey(tileClassName)] ?: true
        }

    suspend fun setTileEnabled(tileClassName: String, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[getTileEnabledKey(tileClassName)] = enabled
        }
    }
}
