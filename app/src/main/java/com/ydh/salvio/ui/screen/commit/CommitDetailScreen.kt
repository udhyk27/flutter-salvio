package com.ydh.salvio.ui.screen.commit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.CommitFile
import com.ydh.salvio.data.model.GitHubCommitDetail
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitDetailScreen(
    owner: String,
    repoName: String,
    sha: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.commitDetailState.collectAsState()

    LaunchedEffect(sha) {
        dashboardViewModel.loadCommitDetail(owner, repoName, sha)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = sha.take(7),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GitHubRed, modifier = Modifier.size(48.dp))
                        Text("커밋 정보를 불러오지 못했습니다.", color = GitHubTextSecondary)
                        Button(onClick = { dashboardViewModel.loadCommitDetail(owner, repoName, sha) }) { Text("다시 시도") }
                    }
                }
            }
            state.detail != null -> {
                CommitDetailContent(
                    detail = state.detail!!,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun CommitDetailContent(detail: GitHubCommitDetail, modifier: Modifier = Modifier) {
    var expandedFiles by remember { mutableStateOf<Set<String>>(emptySet()) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 커밋 헤더
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GitHubBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = detail.commit.message,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = GitHubBorder)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = detail.author?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(GitHubBorder)
                        )
                        Column {
                            Text(
                                text = detail.commit.author.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatDate(detail.commit.author.date),
                                fontSize = 12.sp,
                                color = GitHubTextSecondary
                            )
                        }
                    }
                    // 통계
                    detail.stats?.let { stats ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            StatChip(label = "+${stats.additions}", color = GitHubGreen)
                            StatChip(label = "-${stats.deletions}", color = GitHubRed)
                            StatChip(label = "${detail.files.size} 파일", color = GitHubTextSecondary)
                        }
                    }
                    Text(
                        text = detail.sha,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 파일 목록
        item {
            Text(
                text = "변경 파일 ${detail.files.size}개",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(detail.files) { file ->
            val isExpanded = expandedFiles.contains(file.filename)
            FileCard(
                file = file,
                isExpanded = isExpanded,
                onToggle = {
                    expandedFiles = if (isExpanded) expandedFiles - file.filename
                    else expandedFiles + file.filename
                }
            )
        }
    }
}

@Composable
private fun FileCard(file: CommitFile, isExpanded: Boolean, onToggle: () -> Unit) {
    val statusColor = when (file.status) {
        "added" -> GitHubGreen
        "removed" -> GitHubRed
        "modified" -> GitHubBlue
        "renamed" -> GitHubYellow
        else -> GitHubTextSecondary
    }
    val statusLabel = when (file.status) {
        "added" -> "A"
        "removed" -> "D"
        "modified" -> "M"
        "renamed" -> "R"
        else -> "?"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column {
            // 파일 헤더 (항상 표시)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = file.filename,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
                Text(text = "+${file.additions}", fontSize = 12.sp, color = GitHubGreen)
                Text(text = "-${file.deletions}", fontSize = 12.sp, color = GitHubRed)
                if (file.patch != null) {
                    IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = GitHubTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Diff 내용 (펼쳤을 때만 표시)
            if (isExpanded && file.patch != null) {
                HorizontalDivider(color = GitHubBorder)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117))
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Column {
                        file.patch.lines().forEach { line ->
                            val lineColor = when {
                                line.startsWith("+") -> GitHubGreen.copy(alpha = 0.15f)
                                line.startsWith("-") -> GitHubRed.copy(alpha = 0.15f)
                                line.startsWith("@@") -> GitHubBlue.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            }
                            val textColor = when {
                                line.startsWith("+") -> GitHubGreen
                                line.startsWith("-") -> GitHubRed
                                line.startsWith("@@") -> GitHubBlue
                                else -> GitHubText
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(lineColor)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = line,
                                    fontSize = 11.sp,
                                    color = textColor,
                                    fontFamily = FontFamily.Monospace,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun formatDate(dateStr: String): String = try {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm").withZone(ZoneId.systemDefault())
    formatter.format(Instant.parse(dateStr))
} catch (e: Exception) { dateStr }
