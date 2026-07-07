package com.ydh.salvio.ui.screen.release

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ydh.salvio.data.model.GitHubRelease
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.releaseState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(owner, repoName) {
        dashboardViewModel.loadReleases(owner, repoName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Releases", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadReleases(owner, repoName) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            state.isLoading && state.releases.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null && state.releases.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GitHubRed, modifier = Modifier.size(48.dp))
                    Text("릴리즈 목록을 불러오지 못했습니다.", color = GitHubTextSecondary)
                    Button(onClick = { dashboardViewModel.loadReleases(owner, repoName) }) { Text("다시 시도") }
                }
            }

            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { dashboardViewModel.loadReleases(owner, repoName) },
                modifier = Modifier.padding(paddingValues)
            ) {
                if (state.releases.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("릴리즈가 없습니다.", color = GitHubTextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.releases) { release ->
                            ReleaseCard(
                                release = release,
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)))
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(release: GitHubRelease, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = GitHubBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = release.tagName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GitHubBlue,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                if (release.prerelease) {
                    ReleaseBadge(label = "Pre-release", color = GitHubYellow)
                }
                if (release.draft) {
                    ReleaseBadge(label = "Draft", color = GitHubTextSecondary)
                }
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
            }

            release.name?.takeIf { it.isNotBlank() && it != release.tagName }?.let { name ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = release.author.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape)
                )
                Text(text = release.author.login, fontSize = 12.sp, color = GitHubTextSecondary)
                Text(
                    text = release.publishedAt?.let { formatReleaseDate(it) } ?: "",
                    fontSize = 12.sp,
                    color = GitHubTextSecondary
                )
            }

            release.body?.takeIf { it.isNotBlank() }?.let { body ->
                val cleanBody = body.replace(Regex("#{1,6}\\s"), "").replace("**", "").replace("*", "").trim()
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = cleanBody.lines().take(4).joinToString("\n"),
                    fontSize = 12.sp,
                    color = GitHubTextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (release.assets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GitHubBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${release.assets.size}개 에셋",
                        fontSize = 12.sp,
                        color = GitHubTextSecondary
                    )
                    val totalDownloads = release.assets.sumOf { it.downloadCount }
                    if (totalDownloads > 0) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${formatCount(totalDownloads)} 다운로드",
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun formatReleaseDate(dateStr: String): String {
    return try {
        val instant = Instant.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateStr.take(10)
    }
}

private fun formatCount(count: Int): String =
    if (count >= 1000) "${count / 1000}k" else count.toString()
