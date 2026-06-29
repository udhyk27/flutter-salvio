package com.ydh.salvio.ui.screen.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.ydh.salvio.data.model.CommitWeekActivity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.GitHubContributor
import com.ydh.salvio.ui.component.CommitActivityChart
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.statsState.collectAsState()

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadStats(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("작업 통계", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadStats(owner, repoName) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CommitActivityCard(activity = state.commitActivity)
            }
            item {
                ContributorRankingCard(contributors = state.contributors)
            }
            item {
                RecentCommitAuthorsCard(
                    commits = state.commits.groupBy { it.commit.author.name }
                        .map { (name, commits) -> name to commits.size }
                        .sortedByDescending { it.second }
                        .take(10)
                )
            }
        }
    }
}

@Composable
private fun CommitActivityCard(activity: List<CommitWeekActivity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("커밋 활동 (최근 26주)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val totalRecent = activity.takeLast(4).sumOf { it.total }
            CommitActivityChart(activity = activity)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "최근 4주 총 $totalRecent commits",
                fontSize = 12.sp,
                color = GitHubTextSecondary
            )
        }
    }
}

@Composable
private fun ContributorRankingCard(contributors: List<GitHubContributor>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GitHubYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("기여자 순위", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (contributors.isEmpty()) {
                Text("데이터가 없습니다.", color = GitHubTextSecondary, fontSize = 13.sp)
            } else {
                val maxContributions = contributors.maxOfOrNull { it.contributions } ?: 1

                contributors.take(10).forEachIndexed { index, contributor ->
                    ContributorRow(
                        rank = index + 1,
                        contributor = contributor,
                        maxContributions = maxContributions
                    )
                    if (index < contributors.size - 1) {
                        HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorRow(rank: Int, contributor: GitHubContributor, maxContributions: Int) {
    val rankColor = when (rank) {
        1 -> GitHubYellow
        2 -> GitHubTextSecondary
        3 -> GitHubRed.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "#$rank",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = rankColor,
                modifier = Modifier.width(28.dp)
            )
            AsyncImage(
                model = contributor.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(28.dp).clip(CircleShape).background(GitHubBorder)
            )
            Text(
                text = contributor.login,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${contributor.contributions} commits",
                fontSize = 12.sp,
                color = GitHubTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.padding(start = 38.dp)) {
            val ratio = contributor.contributions.toFloat() / maxContributions.toFloat()
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = when (rank) {
                    1 -> GitHubYellow
                    2 -> GitHubBlue
                    3 -> GitHubGreen
                    else -> GitHubPurple
                },
                trackColor = GitHubBorder
            )
        }
    }
}

@Composable
private fun RecentCommitAuthorsCard(commits: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Commit, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("최근 커밋 작성자", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (commits.isEmpty()) {
                Text("데이터가 없습니다.", color = GitHubTextSecondary, fontSize = 13.sp)
            } else {
                val maxCount = commits.maxOfOrNull { it.second } ?: 1
                commits.forEach { (name, count) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(text = name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(100.dp), maxLines = 1)
                        val ratio = count.toFloat() / maxCount.toFloat()
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = GitHubBlue,
                            trackColor = GitHubBorder
                        )
                        Text(text = "$count", fontSize = 12.sp, color = GitHubTextSecondary, modifier = Modifier.width(30.dp))
                    }
                }
            }
        }
    }
}
