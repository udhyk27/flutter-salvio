package com.ydh.salvio.ui.screen.pullrequest

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.GitHubPullRequest
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRequestScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.prState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Open", "Merged", "Closed")

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadPullRequests(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pull Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadPullRequests(owner, repoName) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> state.openPrs.size
                        1 -> state.mergedPrs.size
                        2 -> state.closedPrs.size
                        else -> 0
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text("$title ($count)", fontSize = 13.sp) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val prs = when (selectedTab) {
                    0 -> state.openPrs
                    1 -> state.mergedPrs
                    2 -> state.closedPrs
                    else -> emptyList()
                }
                val prState = when (selectedTab) {
                    0 -> "open"
                    1 -> "merged"
                    else -> "closed"
                }

                if (prs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("${tabs[selectedTab]} PR이 없습니다.", color = GitHubTextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prs) { pr ->
                            PullRequestCard(pr = pr, state = prState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PullRequestCard(pr: GitHubPullRequest, state: String) {
    val stateColor = when (state) {
        "open" -> GitHubGreen
        "merged" -> Color(0xFF8957E5)
        else -> GitHubRed
    }
    val stateIcon = when (state) {
        "open" -> Icons.Default.MergeType
        "merged" -> Icons.Default.Merge
        else -> Icons.Default.Close
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(stateIcon, contentDescription = null, tint = stateColor, modifier = Modifier.size(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pr.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "#${pr.number}", fontSize = 12.sp, color = GitHubTextSecondary)
                        AsyncImage(
                            model = pr.user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Text(text = pr.user.login, fontSize = 12.sp, color = GitHubTextSecondary)
                        Text(text = formatPrDate(pr, state), fontSize = 12.sp, color = GitHubTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Comment, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
                    Text(text = "${pr.comments + pr.reviewComments}", fontSize = 12.sp, color = GitHubTextSecondary)
                }
                BranchChip(label = pr.base.ref)
                Spacer(modifier = Modifier.weight(1f))
                pr.changedFiles?.let {
                    Text(text = "$it 파일", fontSize = 12.sp, color = GitHubTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BranchChip(label: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = GitHubBorder
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = GitHubBlue,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

private fun formatPrDate(pr: GitHubPullRequest, state: String): String {
    val dateStr = when (state) {
        "merged" -> pr.mergedAt
        "closed" -> pr.closedAt
        else -> pr.createdAt
    } ?: pr.createdAt

    return try {
        val instant = Instant.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("MM월 dd일").withZone(ZoneId.systemDefault())
        val prefix = when (state) {
            "merged" -> "merged "
            "closed" -> "closed "
            else -> "opened "
        }
        prefix + formatter.format(instant)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}
