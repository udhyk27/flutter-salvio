package com.ydh.salvio.ui.screen.search

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ydh.salvio.data.model.CodeSearchItem
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    owner: String,
    repoName: String,
    dashboardViewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by dashboardViewModel.searchState.collectAsState()
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose { dashboardViewModel.clearSearch() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("코드 검색", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // 검색창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("함수명, 변수명, 패턴 검색…", color = GitHubTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GitHubTextSecondary) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "지우기", tint = GitHubTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        dashboardViewModel.searchCode(owner, repoName, query)
                    }),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = GitHubBorder
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        dashboardViewModel.searchCode(owner, repoName, query)
                    },
                    enabled = query.isNotBlank() && !state.isLoading,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("검색")
                }
            }

            // 검색 힌트
            if (state.results == null && !state.isLoading && state.error == null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("검색 팁", fontSize = 12.sp, color = GitHubTextSecondary, fontWeight = FontWeight.SemiBold)
                    listOf(
                        "함수명 또는 클래스명으로 검색",
                        "파일 확장자 지정: `extension:kt`",
                        "특정 경로: `path:src/main`",
                        "언어 지정: `language:kotlin`"
                    ).forEach { tip ->
                        Text("• $tip", fontSize = 12.sp, color = GitHubTextSecondary)
                    }
                }
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GitHubRed, modifier = Modifier.size(40.dp))
                        Text(state.error!!, color = GitHubTextSecondary)
                    }
                }

                state.results != null -> {
                    val results = state.results!!
                    if (results.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(40.dp))
                                Text("\"${state.query}\"에 대한 결과가 없습니다.", color = GitHubTextSecondary)
                            }
                        }
                    } else {
                        Column {
                            // 결과 수 헤더
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${results.totalCount}개 결과",
                                    fontSize = 12.sp,
                                    color = GitHubTextSecondary
                                )
                                if (results.incompleteResults) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GitHubYellow.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, GitHubYellow.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "일부 결과만 표시",
                                            fontSize = 10.sp,
                                            color = GitHubYellow,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(results.items) { item ->
                                    SearchResultCard(item = item, query = state.query)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: CodeSearchItem, query: String) {
    val context = LocalContext.current
    val ext = item.name.substringAfterLast('.', "")
    val extColor = when (ext.lowercase()) {
        "kt", "kts" -> GitHubBlue
        "java" -> GitHubYellow
        "py" -> GitHubGreen
        "js", "ts", "tsx" -> GitHubYellow
        "xml" -> GitHubRed
        "json" -> GitHubPurple
        "md" -> GitHubTextSecondary
        else -> GitHubTextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.htmlUrl)))
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GitHubBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = extColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, extColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (ext.isNotEmpty()) ext.uppercase() else "FILE",
                        fontSize = 9.sp,
                        color = extColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GitHubBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = GitHubTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.path,
                fontSize = 12.sp,
                color = GitHubTextSecondary,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
