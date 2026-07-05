package com.ydh.salvio.ui.screen.issue

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
import com.ydh.salvio.data.model.GitHubIssue
import com.ydh.salvio.data.model.GitHubLabel
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.issueState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadIssues(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issues", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadIssues(owner, repoName) }) {
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
                listOf("Open" to state.openIssues.size, "Closed" to state.closedIssues.size)
                    .forEachIndexed { index, (label, count) ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text("$label ($count)", fontSize = 13.sp) }
                        )
                    }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val issues = if (selectedTab == 0) state.openIssues else state.closedIssues
                if (issues.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (selectedTab == 0) "열린 이슈가 없습니다." else "닫힌 이슈가 없습니다.",
                            color = GitHubTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(issues) { issue ->
                            IssueCard(issue = issue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueCard(issue: GitHubIssue) {
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
                Icon(
                    imageVector = if (issue.state == "open") Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (issue.state == "open") GitHubGreen else Color(0xFF8957E5),
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = issue.title,
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
                        Text(text = "#${issue.number}", fontSize = 12.sp, color = GitHubTextSecondary)
                        AsyncImage(
                            model = issue.user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(CircleShape)
                        )
                        Text(text = issue.user.login, fontSize = 12.sp, color = GitHubTextSecondary)
                        Text(
                            text = formatIssueDate(issue),
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                    }
                }
            }

            if (issue.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    issue.labels.take(4).forEach { label ->
                        LabelChip(label)
                    }
                }
            }

            if (issue.milestone != null || issue.comments > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    issue.milestone?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(12.dp))
                            Text(text = it.title, fontSize = 11.sp, color = GitHubTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (issue.comments > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Comment, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(12.dp))
                            Text(text = "${issue.comments}", fontSize = 11.sp, color = GitHubTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelChip(label: GitHubLabel) {
    val labelColor = try {
        Color(android.graphics.Color.parseColor("#${label.color}"))
    } catch (e: Exception) {
        GitHubBorder
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = labelColor.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, labelColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = label.name,
            fontSize = 10.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

private fun formatIssueDate(issue: GitHubIssue): String {
    val dateStr = issue.closedAt ?: issue.createdAt
    return try {
        val instant = Instant.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("MM월 dd일").withZone(ZoneId.systemDefault())
        val prefix = if (issue.state == "closed") "closed " else "opened "
        prefix + formatter.format(instant)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}
