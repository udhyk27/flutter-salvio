package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.model.GitHubNotification
import com.ydh.salvio.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<GitHubNotification> = emptyList(),
    val error: String? = null
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = (application as SalvioApplication).tokenDataStore

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    private suspend fun getRepo(): GitHubRepository? {
        val token = dataStore.token.first() ?: return null
        return GitHubRepository(RetrofitClient.create(token))
    }

    fun load() {
        viewModelScope.launch {
            _state.value = NotificationUiState(isLoading = true)
            val repo = getRepo() ?: return@launch
            repo.getNotifications().fold(
                onSuccess = { _state.value = NotificationUiState(notifications = it) },
                onFailure = { _state.value = NotificationUiState(error = it.message) }
            )
        }
    }

    fun markRead(threadId: String) {
        viewModelScope.launch {
            val repo = getRepo() ?: return@launch
            repo.markNotificationRead(threadId).getOrNull()
            _state.value = _state.value.copy(
                notifications = _state.value.notifications.map {
                    if (it.id == threadId) it.copy(unread = false) else it
                }
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val repo = getRepo() ?: return@launch
            _state.value.notifications.filter { it.unread }.forEach { n ->
                repo.markNotificationRead(n.id)
            }
            _state.value = _state.value.copy(
                notifications = _state.value.notifications.map { it.copy(unread = false) }
            )
        }
    }
}
