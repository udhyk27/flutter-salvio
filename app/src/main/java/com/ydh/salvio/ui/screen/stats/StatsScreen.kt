package com.ydh.salvio.ui.screen.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.ydh.salvio.data.model.CommitWeekActivity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.ContributorWeeklyStats
import com.ydh.salvio.data.model.GitHubContributor
import com.ydh.salvio.data.model.TrafficClones
import com.ydh.salvio.data.model.TrafficViews
import com.ydh.salvio.ui.component.CommitActivityChart
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel

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
        when {
            state.isLoading && state.contributors.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.contributors.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GitHubRed, modifier = Modifier.size(48.dp))
                        Text("통계를 불러오지 못했습니다.", color = GitHubTextSecondary)
                        Button(onClick = { dashboardViewModel.loadStats(owner, repoName) }) { Text("다시 시도") }
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { dashboardViewModel.loadStats(owner, repoName) },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { CommitActivityCard(activity = state.commitActivity) }
                        if (state.trafficViews != null || state.trafficClones != null) {
                            item {
                                TrafficCard(
                                    views = state.trafficViews,
                                    clones = state.trafficClones
                                )
                            }
                        } else if (state.trafficAccessDenied) {
                            item { TrafficDeniedCard() }
                        }
                        item {
                            ContributorRankingCard(
                                contributors = state.contributors,
                                weeklyStats = state.contributorWeeklyStats
                            )
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
private fun TrafficCard(views: TrafficViews?, clones: TrafficClones?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("저장소 트래픽 (최근 14일)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                views?.let {
                    TrafficStatItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Visibility,
                        label = "조회수",
                        total = it.count,
                        uniques = it.uniques,
                        color = GitHubBlue
                    )
                }
                clones?.let {
                    TrafficStatItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Download,
                        label = "클론 수",
                        total = it.count,
                        uniques = it.uniques,
                        color = GitHubGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun TrafficDeniedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(18.dp))
            Column {
                Text("트래픽 통계 없음", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("저장소 push 권한이 있어야 조회할 수 있습니다.", fontSize = 12.sp, color = GitHubTextSecondary)
            }
        }
    }
}

@Composable
private fun TrafficStatItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    total: Int,
    uniques: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(text = label, fontSize = 12.sp, color = GitHubTextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$total", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = "순방문자 $uniques", fontSize = 11.sp, color = GitHubTextSecondary)
        }
    }
}

@Composable
private fun ContributorRankingCard(
    contributors: List<GitHubContributor>,
    weeklyStats: List<ContributorWeeklyStats> = emptyList()
) {
    val statsMap = weeklyStats.associate { it.author.login to it }
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
                        maxContributions = maxContributions,
                        weeklyStats = statsMap[contributor.login]
                    )
                    if (index < contributors.take(10).size - 1) {
                        HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorRow(
    rank: Int,
    contributor: GitHubContributor,
    maxContributions: Int,
    weeklyStats: ContributorWeeklyStats? = null
) {
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${contributor.contributions} commits",
                    fontSize = 12.sp,
                    color = GitHubTextSecondary
                )
                weeklyStats?.let { ws ->
                    val totalAdditions = ws.weeks.sumOf { it.a }
                    val totalDeletions = ws.weeks.sumOf { it.d }
                    if (totalAdditions > 0 || totalDeletions > 0) {
                        Text(
                            text = "+$totalAdditions / -$totalDeletions",
                            fontSize = 10.sp,
                            color = GitHubTextSecondary
                        )
                    }
                }
            }
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
