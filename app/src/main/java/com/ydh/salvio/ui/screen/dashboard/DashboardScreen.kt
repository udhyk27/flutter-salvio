package com.ydh.salvio.ui.screen.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.GitHubCommit
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardUiState
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onNavigateToPRs: () -> Unit,
    onNavigateToBranches: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToCommit: (sha: String) -> Unit,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.dashboardState.collectAsState()

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadDashboard(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(repoName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(owner, fontSize = 12.sp, color = GitHubTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadDashboard(owner, repoName) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StatsRow(state, onNavigateToPRs, onNavigateToBranches) }
            item { QuickActionsRow(onNavigateToPRs, onNavigateToBranches, onNavigateToStats) }
            item { RecentCommitsSection(state.recentCommits, onNavigateToCommit) }
            item { OpenPRsSection(state, onNavigateToPRs) }
            item { ContributorsSection(state) }
        }
    }
}

@Composable
private fun StatsRow(
    state: DashboardUiState,
    onNavigateToPRs: () -> Unit,
    onNavigateToBranches: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Open PR",
            value = state.stats?.openPrCount?.toString() ?: "-",
            color = GitHubGreen,
            onClick = onNavigateToPRs
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Merged PR",
            value = state.stats?.mergedPrCount?.toString() ?: "-",
            color = GitHubBlue,
            onClick = onNavigateToPRs
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "브랜치",
            value = state.stats?.branchCount?.toString() ?: "-",
            color = GitHubYellow,
            onClick = onNavigateToBranches
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "기여자",
            value = state.stats?.contributorCount?.toString() ?: "-",
            color = GitHubPurple,
            onClick = {}
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = GitHubTextSecondary)
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNavigateToPRs: () -> Unit,
    onNavigateToBranches: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.MergeType,
            label = "Pull Requests",
            onClick = onNavigateToPRs
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AccountTree,
            label = "브랜치",
            onClick = onNavigateToBranches
        )
        ActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.BarChart,
            label = "통계",
            onClick = onNavigateToStats
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, GitHubBorder),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(text = label, fontSize = 11.sp, color = GitHubTextSecondary)
        }
    }
}

@Composable
private fun RecentCommitsSection(commits: List<GitHubCommit>, onCommitClick: (sha: String) -> Unit) {
    SectionCard(title = "최근 커밋", icon = Icons.Default.Commit) {
        if (commits.isEmpty()) {
            Text("커밋이 없습니다.", color = GitHubTextSecondary, fontSize = 13.sp)
        } else {
            commits.forEach { commit ->
                CommitItem(commit, onClick = { onCommitClick(commit.sha) })
                if (commit != commits.last()) HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun CommitItem(commit: GitHubCommit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AsyncImage(
            model = commit.author?.avatarUrl,
            contentDescription = null,
            modifier = Modifier.size(28.dp).clip(CircleShape).background(GitHubBorder)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = commit.commit.message.lines().first(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${commit.commit.author.name} • ${formatDate(commit.commit.author.date)}",
                fontSize = 11.sp,
                color = GitHubTextSecondary
            )
        }
        Text(
            text = commit.sha.take(7),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
private fun OpenPRsSection(state: DashboardUiState, onNavigateToPRs: () -> Unit) {
    SectionCard(
        title = "열린 Pull Requests",
        icon = Icons.Default.MergeType,
        action = if (state.openPrs.isNotEmpty()) "모두 보기" to onNavigateToPRs else null
    ) {
        if (state.openPrs.isEmpty()) {
            Text("열린 PR이 없습니다.", color = GitHubTextSecondary, fontSize = 13.sp)
        } else {
            state.openPrs.forEach { pr ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MergeType, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = pr.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "#${pr.number} • ${pr.user.login}", fontSize = 11.sp, color = GitHubTextSecondary)
                    }
                }
                if (pr != state.openPrs.last()) HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun ContributorsSection(state: DashboardUiState) {
    if (state.contributors.isNotEmpty()) {
        SectionCard(title = "기여자", icon = Icons.Default.People) {
            state.contributors.take(5).forEach { contributor ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = contributor.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(GitHubBorder)
                    )
                    Text(text = contributor.login, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(text = "${contributor.contributions} commits", fontSize = 12.sp, color = GitHubTextSecondary)
                }
                if (contributor != state.contributors.take(5).last()) HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: Pair<String, () -> Unit>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                action?.let { (label, onClick) ->
                    TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val instant = Instant.parse(dateStr)
        val now = Instant.now()
        val days = ChronoUnit.DAYS.between(instant, now)
        when {
            days == 0L -> "오늘"
            days == 1L -> "어제"
            days < 7L -> "${days}일 전"
            days < 30L -> "${days / 7}주 전"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MM/dd").withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    } catch (e: Exception) {
        dateStr.take(10)
    }
}
