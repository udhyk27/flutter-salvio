package com.ydh.salvio.ui.screen.repos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
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
import com.ydh.salvio.ui.theme.GitHubBorder
import com.ydh.salvio.ui.theme.GitHubGreen
import com.ydh.salvio.ui.theme.GitHubTextSecondary
import com.ydh.salvio.ui.theme.GitHubYellow
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
    onLogout: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val repoState by repoViewModel.repoListState.collectAsState()
    val user = (authState as? AuthState.Success)?.user
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repoViewModel.loadRepos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repository", fontWeight = FontWeight.Bold) },
                actions = {
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
                                .clickable { authViewModel.logout(); onLogout() }
                                .padding(end = 8.dp)
                        )
                    } ?: IconButton(onClick = { authViewModel.logout(); onLogout() }) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "로그아웃")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Repository 검색...", color = GitHubTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GitHubBorder,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            when (val state = repoState) {
                is RepoListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RepoListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { repoViewModel.loadRepos() }) { Text("다시 시도") }
                        }
                    }
                }
                is RepoListState.Success -> {
                    val filtered = state.repos.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description?.contains(searchQuery, ignoreCase = true) == true
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { repo ->
                            RepoCard(
                                repo = repo,
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
fun RepoCard(repo: GitHubRepo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (repo.private) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = GitHubTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = GitHubYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = repo.stars.toString(), fontSize = 12.sp, color = GitHubTextSecondary)
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = repo.description,
                    fontSize = 13.sp,
                    color = GitHubTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repo.language?.let { lang ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(languageColor(lang))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = lang, fontSize = 12.sp, color = GitHubTextSecondary)
                    }
                }
                Text(
                    text = "${repo.openIssues} issues",
                    fontSize = 12.sp,
                    color = GitHubTextSecondary
                )
                Text(
                    text = repo.defaultBranch,
                    fontSize = 12.sp,
                    color = GitHubGreen
                )
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
