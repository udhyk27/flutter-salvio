package com.ydh.salvio.ui.screen.repos

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
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
import com.ydh.salvio.data.model.GitHubRepo
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.AuthState
import com.ydh.salvio.viewmodel.AuthViewModel
import com.ydh.salvio.viewmodel.RepoListState
import com.ydh.salvio.viewmodel.RepoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    authViewModel: AuthViewModel,
    repoViewModel: RepoViewModel,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val authState by authViewModel.authState.collectAsState()
    val repoState by repoViewModel.repoListState.collectAsState()
    val favorites by repoViewModel.favoriteRepos.collectAsState()
    val watchedRepos by repoViewModel.watchedRepos.collectAsState()
    val user = (authState as? AuthState.Success)?.user

    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var pendingWatchRepoName by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingWatchRepoName?.let { repoViewModel.toggleWatch(it) }
        }
        pendingWatchRepoName = null
    }

    LaunchedEffect(Unit) { repoViewModel.loadRepos() }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃 하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogout()
                }) { Text("로그아웃", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("취소") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repository", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "알림",
                            tint = GitHubTextSecondary
                        )
                    }
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            if (showFavoritesOnly) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = "즐겨찾기만 보기",
                            tint = if (showFavoritesOnly) GitHubYellow else GitHubTextSecondary
                        )
                    }
                    IconButton(onClick = { repoViewModel.loadRepos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                    user?.let {
                        AsyncImage(
                            model = it.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { showLogoutDialog = true }
                                .padding(end = 8.dp)
                        )
                    } ?: IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "로그아웃")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Repository 검색...", color = GitHubTextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GitHubBorder,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = GitHubTextSecondary)
                        }
                    }
                }
            )

            when (val state = repoState) {
                is RepoListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RepoListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { repoViewModel.loadRepos() }) { Text("다시 시도") }
                        }
                    }
                }
                is RepoListState.Success -> {
                    val allRepos = state.repos
                    val filtered = allRepos.filter {
                        val matchQuery = searchQuery.isBlank() ||
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description?.contains(searchQuery, ignoreCase = true) == true
                        val matchFav = !showFavoritesOnly || favorites.contains(it.fullName)
                        matchQuery && matchFav
                    }

                    val (favRepos, otherRepos) = filtered.partition { favorites.contains(it.fullName) }
                    val sorted = favRepos + otherRepos

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (favRepos.isNotEmpty() && !showFavoritesOnly) {
                            item {
                                Text(
                                    "즐겨찾기",
                                    fontSize = 12.sp,
                                    color = GitHubTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                        items(sorted, key = { it.fullName }) { repo ->
                            val isFavorite = favorites.contains(repo.fullName)
                            val isWatched = watchedRepos.contains(repo.fullName)
                            val showDivider = !showFavoritesOnly && repo == otherRepos.firstOrNull()

                            if (showDivider) {
                                Text(
                                    "전체",
                                    fontSize = 12.sp,
                                    color = GitHubTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                )
                            }
                            RepoCard(
                                repo = repo,
                                isFavorite = isFavorite,
                                isWatched = isWatched,
                                onFavoriteToggle = { repoViewModel.toggleFavorite(repo.fullName) },
                                onWatchToggle = {
                                    if (isWatched) {
                                        repoViewModel.toggleWatch(repo.fullName)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        pendingWatchRepoName = repo.fullName
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        repoViewModel.toggleWatch(repo.fullName)
                                    }
                                },
                                onClick = { onRepoSelected(repo.owner.login, repo.name) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RepoCard(
    repo: GitHubRepo,
    isFavorite: Boolean,
    isWatched: Boolean,
    onFavoriteToggle: () -> Unit,
    onWatchToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isFavorite) GitHubYellow.copy(alpha = 0.5f) else GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (repo.private) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = repo.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onWatchToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isWatched) Icons.Default.Notifications else Icons.Outlined.Notifications,
                        contentDescription = if (isWatched) "알림 끄기" else "PR 알림 켜기",
                        tint = if (isWatched) GitHubBlue else GitHubTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                        tint = if (isFavorite) GitHubYellow else GitHubTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GitHubYellow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(repo.stars.toString(), fontSize = 12.sp, color = GitHubTextSecondary)
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(repo.description, fontSize = 13.sp, color = GitHubTextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                repo.language?.let { lang ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(languageColor(lang)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(lang, fontSize = 12.sp, color = GitHubTextSecondary)
                    }
                }
                Text("${repo.openIssues} issues", fontSize = 12.sp, color = GitHubTextSecondary)
                Text(repo.defaultBranch, fontSize = 12.sp, color = GitHubGreen)
            }
        }
    }
}

private fun languageColor(language: String): Color = when (language) {
    "Kotlin" -> Color(0xFFA97BFF)
    "Java" -> Color(0xFFB07219)
    "Python" -> Color(0xFF3572A5)
    "JavaScript" -> Color(0xFFF1E05A)
    "TypeScript" -> Color(0xFF2B7489)
    "Swift" -> Color(0xFFFF5733)
    "Go" -> Color(0xFF00ADD8)
    "Rust" -> Color(0xFFDEA584)
    "Dart" -> Color(0xFF00B4AB)
    "C++" -> Color(0xFFF34B7D)
    "C#" -> Color(0xFF178600)
    else -> Color(0xFF8B949E)
}
