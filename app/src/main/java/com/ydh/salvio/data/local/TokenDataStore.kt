package com.ydh.salvio.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salvio_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("github_token")
        private val SELECTED_REPOS_KEY = stringPreferencesKey("selected_repos")
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    val selectedRepos: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_REPOS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun saveSelectedRepos(repos: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_REPOS_KEY] = repos.joinToString(",")
        }
    }
}
