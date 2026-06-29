package com.ydh.salvio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ydh.salvio.SalvioApplication
import com.ydh.salvio.data.api.RetrofitClient
import com.ydh.salvio.data.model.*
import kotlinx.coroutines.flow.update
import com.ydh.salvio.data.repository.GitHubRepository
import kotlinx.coroutines.async
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
    val prReviews: Map<Int, List<GitHubPrReview>> = emptyMap(),
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
    val commitActivity: List<CommitWeekActivity> = emptyList(),
    val error: String? = null
)

data class CommitDetailUiState(
    val isLoading: Boolean = false,
    val detail: GitHubCommitDetail? = null,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalvioApplication
    private val dataStore = app.tokenDataStore

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _prState = MutableStateFlow(PrUiState())
    val prState: StateFlow<PrUiState> = _prState.asStateFlow()

    private val _branchState = MutableStateFlow(BranchUiState())
    val branchState: StateFlow<BranchUiState> = _branchState.asStateFlow()

    private val _statsState = MutableStateFlow(StatsUiState())
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    private val _commitDetailState = MutableStateFlow(CommitDetailUiState())
    val commitDetailState: StateFlow<CommitDetailUiState> = _commitDetailState.asStateFlow()

    private suspend fun getRepo(): GitHubRepository? {
        val token = dataStore.token.first() ?: return null
        return GitHubRepository(RetrofitClient.create(token), app.database.cacheDao())
    }

    fun loadDashboard(owner: String, repoName: String) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, error = null)
            val repo = getRepo() ?: return@launch

            val repoInfo = async { repo.getRepo(owner, repoName) }
            val commits = async { repo.getCommits(owner, repoName) }
            val openPrs = async { repo.getPullRequests(owner, repoName, "open") }
            val branches = async { repo.getBranches(owner, repoName) }
            val contributors = async { repo.getContributors(owner, repoName) }
            val closedPrs = async { repo.getPullRequests(owner, repoName, "closed") }

            val closedList = closedPrs.await().getOrElse { emptyList() }

            _dashboardState.value = DashboardUiState(
                isLoading = false,
                repo = repoInfo.await().getOrNull(),
                stats = GitHubRepoStats(
                    repoFullName = "$owner/$repoName",
                    commitCount = commits.await().getOrElse { emptyList() }.size,
                    openPrCount = openPrs.await().getOrElse { emptyList() }.size,
                    mergedPrCount = closedList.count { it.mergedAt != null },
                    closedPrCount = closedList.count { it.mergedAt == null },
                    branchCount = branches.await().getOrElse { emptyList() }.size,
                    contributorCount = contributors.await().getOrElse { emptyList() }.size
                ),
                recentCommits = commits.await().getOrElse { emptyList() }.take(5),
                openPrs = openPrs.await().getOrElse { emptyList() }.take(3),
                branches = branches.await().getOrElse { emptyList() },
                contributors = contributors.await().getOrElse { emptyList() },
                error = repoInfo.await().exceptionOrNull()?.message
            )
        }
    }

    fun loadPullRequests(owner: String, repoName: String) {
        viewModelScope.launch {
            _prState.value = PrUiState(isLoading = true)
            val repo = getRepo() ?: return@launch

            val open = async { repo.getPullRequests(owner, repoName, "open") }
            val closed = async { repo.getPullRequests(owner, repoName, "closed") }

            val openList = open.await().getOrElse { emptyList() }
            val closedList = closed.await().getOrElse { emptyList() }

            _prState.value = _prState.value.copy(
                isLoading = false,
                openPrs = openList,
                closedPrs = closedList.filter { it.mergedAt == null },
                mergedPrs = closedList.filter { it.mergedAt != null },
                error = open.await().exceptionOrNull()?.message
            )

            // 리뷰 상태 비동기 로드
            loadPrReviews(owner, repoName, openList)
        }
    }

    private fun loadPrReviews(owner: String, repoName: String, prs: List<GitHubPullRequest>) {
        viewModelScope.launch {
            val repo = getRepo() ?: return@launch
            val reviewMap = mutableMapOf<Int, List<GitHubPrReview>>()

            prs.take(20).forEach { pr ->
                repo.getPrReviews(owner, repoName, pr.number).getOrNull()?.let { reviews ->
                    reviewMap[pr.number] = reviews
                }
            }

            _prState.value = _prState.value.copy(prReviews = reviewMap)
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

    fun loadCommitDetail(owner: String, repoName: String, sha: String) {
        viewModelScope.launch {
            _commitDetailState.value = CommitDetailUiState(isLoading = true)
            val repo = getRepo() ?: return@launch
            repo.getCommitDetail(owner, repoName, sha).fold(
                onSuccess = { _commitDetailState.value = CommitDetailUiState(detail = it) },
                onFailure = { _commitDetailState.value = CommitDetailUiState(error = it.message) }
            )
        }
    }

    fun loadStats(owner: String, repoName: String) {
        viewModelScope.launch {
            _statsState.value = StatsUiState(isLoading = true)
            val repo = getRepo() ?: return@launch

            val contributors = async { repo.getContributors(owner, repoName) }
            val commits = async { repo.getCommits(owner, repoName, 1) }
            val activity = async { repo.getCommitActivity(owner, repoName) }

            _statsState.value = StatsUiState(
                isLoading = false,
                contributors = contributors.await().getOrElse { emptyList() },
                commits = commits.await().getOrElse { emptyList() },
                commitActivity = activity.await().getOrElse { emptyList() },
                error = contributors.await().exceptionOrNull()?.message
            )
        }
    }
}
