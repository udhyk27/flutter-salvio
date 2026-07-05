package com.ydh.salvio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_repos")
data class CachedRepo(
    @PrimaryKey val fullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_commits")
data class CachedCommit(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_prs")
data class CachedPr(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val state: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_branches")
data class CachedBranch(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_pr_reviews")
data class CachedPrReview(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val prNumber: Int,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_commit_activity")
data class CachedCommitActivity(
    @PrimaryKey val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_issues")
data class CachedIssue(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val state: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_releases")
data class CachedRelease(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_check_runs")
data class CachedCheckRuns(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val ref: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_pr_files")
data class CachedPrFiles(
    @PrimaryKey val id: String,
    val repoFullName: String,
    val prNumber: Int,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_traffic_views")
data class CachedTrafficViews(
    @PrimaryKey val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_traffic_clones")
data class CachedTrafficClones(
    @PrimaryKey val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_contributor_stats")
data class CachedContributorStats(
    @PrimaryKey val repoFullName: String,
    val json: String,
    val cachedAt: Long = System.currentTimeMillis()
)
