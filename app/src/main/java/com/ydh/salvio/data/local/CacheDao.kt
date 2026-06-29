package com.ydh.salvio.data.local

import androidx.room.*

@Dao
interface CacheDao {

    // Repos
    @Query("SELECT * FROM cached_repos WHERE fullName = :fullName")
    suspend fun getRepo(fullName: String): CachedRepo?

    @Query("SELECT * FROM cached_repos ORDER BY cachedAt DESC")
    suspend fun getAllRepos(): List<CachedRepo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: CachedRepo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepos(repos: List<CachedRepo>)

    // Commits
    @Query("SELECT * FROM cached_commits WHERE repoFullName = :repoFullName ORDER BY cachedAt DESC")
    suspend fun getCommits(repoFullName: String): List<CachedCommit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommits(commits: List<CachedCommit>)

    @Query("DELETE FROM cached_commits WHERE repoFullName = :repoFullName")
    suspend fun deleteCommits(repoFullName: String)

    // PRs
    @Query("SELECT * FROM cached_prs WHERE repoFullName = :repoFullName AND state = :state")
    suspend fun getPrs(repoFullName: String, state: String): List<CachedPr>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrs(prs: List<CachedPr>)

    @Query("DELETE FROM cached_prs WHERE repoFullName = :repoFullName AND state = :state")
    suspend fun deletePrs(repoFullName: String, state: String)

    // Branches
    @Query("SELECT * FROM cached_branches WHERE repoFullName = :repoFullName")
    suspend fun getBranches(repoFullName: String): List<CachedBranch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<CachedBranch>)

    @Query("DELETE FROM cached_branches WHERE repoFullName = :repoFullName")
    suspend fun deleteBranches(repoFullName: String)

    // PR Reviews
    @Query("SELECT * FROM cached_pr_reviews WHERE repoFullName = :repoFullName AND prNumber = :prNumber")
    suspend fun getPrReviews(repoFullName: String, prNumber: Int): List<CachedPrReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrReviews(reviews: List<CachedPrReview>)

    @Query("DELETE FROM cached_pr_reviews WHERE repoFullName = :repoFullName AND prNumber = :prNumber")
    suspend fun deletePrReviews(repoFullName: String, prNumber: Int)

    // Commit Activity
    @Query("SELECT * FROM cached_commit_activity WHERE repoFullName = :repoFullName")
    suspend fun getCommitActivity(repoFullName: String): CachedCommitActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitActivity(activity: CachedCommitActivity)
}
