package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.model.GitHubRepo
import com.ydh.salvio.data.worker.PrCheckWorker
import com.ydh.salvio.util.toUserMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class RepoListState {
    object Idle : RepoListState()
    object Loading : RepoListState()
    data class Success(val repos: List<GitHubRepo>) : RepoListState()
    data class Error(val message: String) : RepoListState()
}

class RepoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalvioApplication
    private val dataStore = app.tokenDataStore

    private val _repoListState = MutableStateFlow<RepoListState>(RepoListState.Idle)
    val repoListState: StateFlow<RepoListState> = _repoListState.asStateFlow()

    private val _selectedRepo = MutableStateFlow<GitHubRepo?>(null)
    val selectedRepo: StateFlow<GitHubRepo?> = _selectedRepo.asStateFlow()

    val favoriteRepos: StateFlow<Set<String>> = dataStore.favoriteRepos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val watchedRepos: StateFlow<List<String>> = dataStore.watchedRepos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun loadRepos() {
        viewModelScope.launch {
            _repoListState.value = RepoListState.Loading
            val token = dataStore.token.first() ?: return@launch
            val repo = app.githubRepository(token)
            repo.getUserRepos().fold(
                onSuccess = { repos -> _repoListState.value = RepoListState.Success(repos) },
                onFailure = { e -> _repoListState.value = RepoListState.Error(e.toUserMessage("조회에 실패했습니다.")) }
            )
        }
    }

    fun selectRepo(repo: GitHubRepo) {
        _selectedRepo.value = repo
    }

    fun toggleFavorite(repoFullName: String) {
        viewModelScope.launch { dataStore.toggleFavorite(repoFullName) }
    }

    fun toggleWatch(repoFullName: String) {
        viewModelScope.launch {
            val current = dataStore.watchedRepos.first().toMutableList()
            if (current.contains(repoFullName)) {
                current.remove(repoFullName)
            } else {
                current.add(repoFullName)
            }
            dataStore.setWatchedRepos(current)

            if (current.isNotEmpty()) {
                PrCheckWorker.schedule(app)
            } else {
                PrCheckWorker.cancel(app)
            }
        }
    }
}
