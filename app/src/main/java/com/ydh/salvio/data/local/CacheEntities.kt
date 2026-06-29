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
