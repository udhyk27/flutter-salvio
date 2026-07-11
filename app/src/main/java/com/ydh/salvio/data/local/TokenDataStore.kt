package com.ydh.salvio.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salvio_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("github_token")
        private val SELECTED_REPOS_KEY = stringPreferencesKey("selected_repos")
        private val FAVORITE_REPOS_KEY = stringPreferencesKey("favorite_repos")
        private val WATCHED_REPOS_KEY = stringPreferencesKey("watched_repos")
        private val LAST_PR_IDS_KEY = stringPreferencesKey("last_pr_ids")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    // 테마 모드 (SYSTEM / LIGHT / DARK). 미설정 시 null → SYSTEM으로 해석
    val themeMode: Flow<String?> = context.dataStore.data.map { it[THEME_MODE_KEY] }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE_KEY] = mode }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    val selectedRepos: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_REPOS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun saveSelectedRepos(repos: List<String>) {
        context.dataStore.edit { it[SELECTED_REPOS_KEY] = repos.joinToString(",") }
    }

    // 즐겨찾기
    val favoriteRepos: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITE_REPOS_KEY]?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    suspend fun toggleFavorite(repoFullName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_REPOS_KEY]
                ?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            if (current.contains(repoFullName)) current.remove(repoFullName)
            else current.add(repoFullName)
            prefs[FAVORITE_REPOS_KEY] = current.joinToString(",")
        }
    }

    // 알림 감시 대상 repo
    val watchedRepos: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[WATCHED_REPOS_KEY]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setWatchedRepos(repos: List<String>) {
        context.dataStore.edit { it[WATCHED_REPOS_KEY] = repos.joinToString(",") }
    }

    // PR 알림: repoFullName -> 마지막으로 알림 보낸 open PR 수 (JSON 형태로 저장)
    suspend fun getLastPrCount(repoFullName: String): Int {
        val raw = context.dataStore.data.first()[LAST_PR_IDS_KEY] ?: return 0
        return raw.split(";")
            .mapNotNull { entry ->
                val parts = entry.split("=")
                if (parts.size == 2 && parts[0] == repoFullName) parts[1].toIntOrNull() else null
            }
            .firstOrNull() ?: 0
    }

    suspend fun saveLastPrCount(repoFullName: String, count: Int) {
        context.dataStore.edit { prefs ->
            val raw = prefs[LAST_PR_IDS_KEY] ?: ""
            val entries = raw.split(";").filter { it.isNotBlank() }
                .filter { !it.startsWith("$repoFullName=") }
                .toMutableList()
            entries.add("$repoFullName=$count")
            prefs[LAST_PR_IDS_KEY] = entries.joinToString(";")
        }
    }
}
