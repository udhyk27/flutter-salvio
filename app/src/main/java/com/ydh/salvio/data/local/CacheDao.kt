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

    // Issues
    @Query("SELECT * FROM cached_issues WHERE repoFullName = :repoFullName AND state = :state")
    suspend fun getIssues(repoFullName: String, state: String): List<CachedIssue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<CachedIssue>)

    @Query("DELETE FROM cached_issues WHERE repoFullName = :repoFullName AND state = :state")
    suspend fun deleteIssues(repoFullName: String, state: String)

    // Releases
    @Query("SELECT * FROM cached_releases WHERE repoFullName = :repoFullName ORDER BY cachedAt DESC")
    suspend fun getReleases(repoFullName: String): List<CachedRelease>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReleases(releases: List<CachedRelease>)

    @Query("DELETE FROM cached_releases WHERE repoFullName = :repoFullName")
    suspend fun deleteReleases(repoFullName: String)

    // Check Runs
    @Query("SELECT * FROM cached_check_runs WHERE id = :id")
    suspend fun getCheckRuns(id: String): CachedCheckRuns?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckRuns(checkRuns: CachedCheckRuns)

    // PR Files
    @Query("SELECT * FROM cached_pr_files WHERE repoFullName = :repoFullName AND prNumber = :prNumber")
    suspend fun getPrFiles(repoFullName: String, prNumber: Int): CachedPrFiles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrFiles(prFiles: CachedPrFiles)

    // Traffic Views
    @Query("SELECT * FROM cached_traffic_views WHERE repoFullName = :repoFullName")
    suspend fun getTrafficViews(repoFullName: String): CachedTrafficViews?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficViews(traffic: CachedTrafficViews)

    // Traffic Clones
    @Query("SELECT * FROM cached_traffic_clones WHERE repoFullName = :repoFullName")
    suspend fun getTrafficClones(repoFullName: String): CachedTrafficClones?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficClones(traffic: CachedTrafficClones)

    // Contributor Stats
    @Query("SELECT * FROM cached_contributor_stats WHERE repoFullName = :repoFullName")
    suspend fun getContributorStats(repoFullName: String): CachedContributorStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContributorStats(stats: CachedContributorStats)
}
