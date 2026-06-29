package com.ydh.salvio.data.model

import com.google.gson.annotations.SerializedName

data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("public_repos") val publicRepos: Int,
    val followers: Int,
    val following: Int
)

data class GitHubRepo(
    val id: Long,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    val description: String?,
    val private: Boolean,
    val owner: GitHubUser,
    @SerializedName("stargazers_count") val stars: Int,
    @SerializedName("forks_count") val forks: Int,
    @SerializedName("open_issues_count") val openIssues: Int,
    @SerializedName("default_branch") val defaultBranch: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("pushed_at") val pushedAt: String?,
    val language: String?
)

data class GitHubCommit(
    val sha: String,
    val commit: CommitDetail,
    val author: GitHubUser?,
    @SerializedName("html_url") val htmlUrl: String
)

data class CommitDetail(
    val message: String,
    val author: CommitAuthor,
    val committer: CommitAuthor
)

data class CommitAuthor(
    val name: String,
    val email: String,
    val date: String
)

data class GitHubPullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String?,
    val state: String,
    val user: GitHubUser,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("merged_at") val mergedAt: String?,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("html_url") val htmlUrl: String,
    val head: PrBranch,
    val base: PrBranch,
    @SerializedName("review_comments") val reviewComments: Int,
    val comments: Int,
    val additions: Int?,
    val deletions: Int?,
    @SerializedName("changed_files") val changedFiles: Int?
)

data class PrBranch(
    val label: String,
    val ref: String,
    val sha: String
)

data class GitHubBranch(
    val name: String,
    val commit: BranchCommit,
    val protected: Boolean
)

data class BranchCommit(
    val sha: String,
    val url: String
)

data class GitHubContributor(
    val login: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val contributions: Int,
    val id: Long
)

data class GitHubPrReview(
    val id: Long,
    val user: GitHubUser,
    val state: String,
    @SerializedName("submitted_at") val submittedAt: String?,
    val body: String?
)

data class CommitWeekActivity(
    val week: Long,
    val total: Int,
    val days: List<Int>
)

data class GitHubRepoStats(
    val repoFullName: String,
    val commitCount: Int,
    val openPrCount: Int,
    val mergedPrCount: Int,
    val closedPrCount: Int,
    val branchCount: Int,
    val contributorCount: Int
)
