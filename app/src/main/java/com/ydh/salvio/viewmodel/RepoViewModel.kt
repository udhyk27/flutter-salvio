package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.model.GitHubRepo
import com.ydh.salvio.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class RepoListState {
    object Idle : RepoListState()
    object Loading : RepoListState()
    data class Success(val repos: List<GitHubRepo>) : RepoListState()
    data class Error(val message: String) : RepoListState()
}

class RepoViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = (application as SalvioApplication).tokenDataStore

    private val _repoListState = MutableStateFlow<RepoListState>(RepoListState.Idle)
    val repoListState: StateFlow<RepoListState> = _repoListState.asStateFlow()

    private val _selectedRepo = MutableStateFlow<GitHubRepo?>(null)
    val selectedRepo: StateFlow<GitHubRepo?> = _selectedRepo.asStateFlow()

    fun loadRepos() {
        viewModelScope.launch {
            _repoListState.value = RepoListState.Loading
            val token = dataStore.token.first() ?: return@launch
            val api = RetrofitClient.create(token)
            val repo = GitHubRepository(api)
            repo.getUserRepos().fold(
                onSuccess = { repos ->
                    _repoListState.value = RepoListState.Success(repos)
                },
                onFailure = { e ->
                    _repoListState.value = RepoListState.Error(e.message ?: "조회 실패")
                }
            )
        }
    }

    fun selectRepo(repo: GitHubRepo) {
        _selectedRepo.value = repo
    }
}
