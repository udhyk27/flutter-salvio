package com.ydh.salvio.data.repository

import com.ydh.salvio.data.api.GitHubApi
import com.ydh.salvio.data.model.*

class GitHubRepository(private val api: GitHubApi) {

    suspend fun getUser(): Result<GitHubUser> = runCatching { api.getAuthenticatedUser() }

    suspend fun getUserRepos(): Result<List<GitHubRepo>> = runCatching {
        api.getUserRepos(perPage = 100)
    }

    suspend fun getCommits(owner: String, repo: String, page: Int = 1): Result<List<GitHubCommit>> =
        runCatching { api.getCommits(owner, repo, perPage = 30, page = page) }

    suspend fun getPullRequests(owner: String, repo: String, state: String = "open"): Result<List<GitHubPullRequest>> =
        runCatching { api.getPullRequests(owner, repo, state = state, perPage = 50) }

    suspend fun getBranches(owner: String, repo: String): Result<List<GitHubBranch>> =
        runCatching { api.getBranches(owner, repo) }

    suspend fun getContributors(owner: String, repo: String): Result<List<GitHubContributor>> =
        runCatching { api.getContributors(owner, repo) }

    suspend fun getRepo(owner: String, repo: String): Result<GitHubRepo> =
        runCatching { api.getRepo(owner, repo) }

    suspend fun getBranchLatestCommit(owner: String, repo: String, ref: String): Result<GitHubCommit> =
        runCatching { api.getBranchLatestCommit(owner, repo, ref) }

    suspend fun getDashboardStats(owner: String, repo: String): Result<GitHubRepoStats> = runCatching {
        val openPrs = api.getPullRequests(owner, repo, state = "open", perPage = 1)
        val closedPrs = api.getPullRequests(owner, repo, state = "closed", perPage = 100)
        val branches = api.getBranches(owner, repo)
        val contributors = runCatching { api.getContributors(owner, repo) }.getOrElse { emptyList() }
        val mergedCount = closedPrs.count { it.mergedAt != null }
        val closedCount = closedPrs.count { it.mergedAt == null }

        GitHubRepoStats(
            repoFullName = "$owner/$repo",
            commitCount = 0,
            openPrCount = openPrs.size,
            mergedPrCount = mergedCount,
            closedPrCount = closedCount,
            branchCount = branches.size,
            contributorCount = contributors.size
        )
    }
}
