package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.model.GitHubUser
import com.ydh.salvio.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: GitHubUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = (application as SalvioApplication).tokenDataStore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<GitHubUser?>(null)
    val currentUser: StateFlow<GitHubUser?> = _currentUser.asStateFlow()

    init {
        checkExistingToken()
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val token = dataStore.token.first()
            if (!token.isNullOrBlank()) {
                loginWithToken(token)
            }
        }
    }

    fun loginWithToken(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val api = RetrofitClient.create(token)
            val repo = GitHubRepository(api)
            repo.getUser().fold(
                onSuccess = { user ->
                    dataStore.saveToken(token)
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                },
                onFailure = { e ->
                    val msg = when {
                        e.message?.contains("401") == true -> "토큰이 유효하지 않습니다."
                        e.message?.contains("network", ignoreCase = true) == true ||
                        e is java.net.UnknownHostException ||
                        e is java.net.SocketTimeoutException -> "네트워크 연결을 확인하세요."
                        else -> "인증에 실패했습니다. 다시 시도해주세요."
                    }
                    _authState.value = AuthState.Error(msg)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.clearToken()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }
}
