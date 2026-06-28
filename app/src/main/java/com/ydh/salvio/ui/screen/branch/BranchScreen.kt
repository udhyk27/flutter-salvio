package com.ydh.salvio.ui.screen.branch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ydh.salvio.data.model.GitHubBranch
import com.ydh.salvio.data.model.GitHubCommit
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.branchState.collectAsState()

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadBranches(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("브랜치", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadBranches(owner, repoName) }) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "${state.branches.size}개 브랜치",
                    fontSize = 13.sp,
                    color = GitHubTextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(state.branches) { branch ->
                BranchCard(
                    branch = branch,
                    latestCommit = state.branchCommits[branch.name]
                )
            }
        }
    }
}

@Composable
private fun BranchCard(branch: GitHubBranch, latestCommit: GitHubCommit?) {
    val commitDate = latestCommit?.commit?.author?.date
    val isStale = commitDate?.let {
        try {
            val days = ChronoUnit.DAYS.between(Instant.parse(it), Instant.now())
            days > 30
        } catch (e: Exception) { false }
    } ?: false

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isStale) GitHubYellow.copy(alpha = 0.5f) else GitHubBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountTree,
                contentDescription = null,
                tint = if (branch.protected) GitHubYellow else GitHubGreen,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = branch.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (branch.protected) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GitHubYellow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "protected",
                                fontSize = 10.sp,
                                color = GitHubYellow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isStale) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GitHubRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "오래됨",
                                fontSize = 10.sp,
                                color = GitHubRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                latestCommit?.let { commit ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = commit.commit.message.lines().first(),
                        fontSize = 12.sp,
                        color = GitHubTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${commit.commit.author.name} • ${formatDate(commit.commit.author.date)}",
                        fontSize = 11.sp,
                        color = GitHubTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = branch.commit.sha.take(7),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val days = ChronoUnit.DAYS.between(Instant.parse(dateStr), Instant.now())
        when {
            days == 0L -> "오늘"
            days == 1L -> "어제"
            days < 7L -> "${days}일 전"
            days < 30L -> "${days / 7}주 전"
            else -> "${days / 30}개월 전"
        }
    } catch (e: Exception) { dateStr.take(10) }
}
