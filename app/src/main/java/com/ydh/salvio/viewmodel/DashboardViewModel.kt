package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.model.*
import com.ydh.salvio.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val repo: GitHubRepo? = null,
    val stats: GitHubRepoStats? = null,
    val recentCommits: List<GitHubCommit> = emptyList(),
    val openPrs: List<GitHubPullRequest> = emptyList(),
    val branches: List<GitHubBranch> = emptyList(),
    val contributors: List<GitHubContributor> = emptyList(),
    val error: String? = null
)

data class PrUiState(
    val isLoading: Boolean = false,
    val openPrs: List<GitHubPullRequest> = emptyList(),
    val closedPrs: List<GitHubPullRequest> = emptyList(),
    val mergedPrs: List<GitHubPullRequest> = emptyList(),
    val error: String? = null
)

data class BranchUiState(
    val isLoading: Boolean = false,
    val branches: List<GitHubBranch> = emptyList(),
    val branchCommits: Map<String, GitHubCommit> = emptyMap(),
    val error: String? = null
)

data class StatsUiState(
    val isLoading: Boolean = false,
    val contributors: List<GitHubContributor> = emptyList(),
    val commits: List<GitHubCommit> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = (application as SalvioApplication).tokenDataStore

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _prState = MutableStateFlow(PrUiState())
    val prState: StateFlow<PrUiState> = _prState.asStateFlow()

    private val _branchState = MutableStateFlow(BranchUiState())
    val branchState: StateFlow<BranchUiState> = _branchState.asStateFlow()

    private val _statsState = MutableStateFlow(StatsUiState())
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    private suspend fun getRepo(): GitHubRepository? {
        val token = dataStore.token.first() ?: return null
        return GitHubRepository(RetrofitClient.create(token))
    }

    fun loadDashboard(owner: String, repoName: String) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, error = null)
            val repo = getRepo() ?: return@launch

            val repoInfo = repo.getRepo(owner, repoName)
            val commits = repo.getCommits(owner, repoName)
            val openPrs = repo.getPullRequests(owner, repoName, "open")
            val branches = repo.getBranches(owner, repoName)
            val contributors = repo.getContributors(owner, repoName)

            val closedPrs = repo.getPullRequests(owner, repoName, "closed").getOrElse { emptyList() }
            val mergedCount = closedPrs.count { it.mergedAt != null }

            _dashboardState.value = DashboardUiState(
                isLoading = false,
                repo = repoInfo.getOrNull(),
                stats = GitHubRepoStats(
                    repoFullName = "$owner/$repoName",
                    commitCount = commits.getOrElse { emptyList() }.size,
                    openPrCount = openPrs.getOrElse { emptyList() }.size,
                    mergedPrCount = mergedCount,
                    closedPrCount = closedPrs.count { it.mergedAt == null },
                    branchCount = branches.getOrElse { emptyList() }.size,
                    contributorCount = contributors.getOrElse { emptyList() }.size
                ),
                recentCommits = commits.getOrElse { emptyList() }.take(5),
                openPrs = openPrs.getOrElse { emptyList() }.take(3),
                branches = branches.getOrElse { emptyList() },
                contributors = contributors.getOrElse { emptyList() },
                error = repoInfo.exceptionOrNull()?.message
            )
        }
    }

    fun loadPullRequests(owner: String, repoName: String) {
        viewModelScope.launch {
            _prState.value = PrUiState(isLoading = true)
            val repo = getRepo() ?: return@launch

            val open = repo.getPullRequests(owner, repoName, "open")
            val closed = repo.getPullRequests(owner, repoName, "closed")

            val closedList = closed.getOrElse { emptyList() }
            _prState.value = PrUiState(
                isLoading = false,
                openPrs = open.getOrElse { emptyList() },
                closedPrs = closedList.filter { it.mergedAt == null },
                mergedPrs = closedList.filter { it.mergedAt != null },
                error = open.exceptionOrNull()?.message ?: closed.exceptionOrNull()?.message
            )
        }
    }

    fun loadBranches(owner: String, repoName: String) {
        viewModelScope.launch {
            _branchState.value = BranchUiState(isLoading = true)
            val repo = getRepo() ?: return@launch

            val branches = repo.getBranches(owner, repoName).getOrElse { emptyList() }
            val commitMap = mutableMapOf<String, GitHubCommit>()

            branches.forEach { branch ->
                repo.getBranchLatestCommit(owner, repoName, branch.name).getOrNull()?.let {
                    commitMap[branch.name] = it
                }
            }

            _branchState.value = BranchUiState(
                isLoading = false,
                branches = branches,
                branchCommits = commitMap
            )
        }
    }

    fun loadStats(owner: String, repoName: String) {
        viewModelScope.launch {
            _statsState.value = StatsUiState(isLoading = true)
            val repo = getRepo() ?: return@launch

            val contributors = repo.getContributors(owner, repoName)
            val commits = repo.getCommits(owner, repoName, 1)

            _statsState.value = StatsUiState(
                isLoading = false,
                contributors = contributors.getOrElse { emptyList() },
                commits = commits.getOrElse { emptyList() },
                error = contributors.exceptionOrNull()?.message
            )
        }
    }
}
