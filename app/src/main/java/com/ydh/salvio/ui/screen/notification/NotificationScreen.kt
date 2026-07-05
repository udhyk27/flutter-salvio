package com.ydh.salvio.ui.screen.notification

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ydh.salvio.data.model.GitHubNotification
import com.ydh.salvio.ui.theme.*
import com.ydh.salvio.viewmodel.NotificationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val state by notificationViewModel.state.collectAsState()
    val unreadCount = state.notifications.count { it.unread }

    LaunchedEffect(Unit) { notificationViewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("알림", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Badge { Text("$unreadCount") }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로") }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = { notificationViewModel.markAllRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "모두 읽음", tint = GitHubBlue)
                        }
                    }
                    IconButton(onClick = { notificationViewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { Text("알림을 불러오지 못했습니다.", color = GitHubTextSecondary) }

            state.notifications.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = GitHubTextSecondary, modifier = Modifier.size(48.dp))
                    Text("읽지 않은 알림이 없습니다.", color = GitHubTextSecondary)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkRead = { notificationViewModel.markRead(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: GitHubNotification, onMarkRead: () -> Unit) {
    val (typeIcon, typeColor) = when (notification.subject.type) {
        "PullRequest" -> Icons.Default.MergeType to GitHubBlue
        "Issue" -> Icons.Default.BugReport to GitHubGreen
        "Release" -> Icons.Default.LocalOffer to GitHubPurple
        "Commit" -> Icons.Default.Commit to GitHubTextSecondary
        else -> Icons.Default.Notifications to GitHubTextSecondary
    }

    val reasonLabel = when (notification.reason) {
        "review_requested" -> "리뷰 요청"
        "assign" -> "담당자 지정"
        "mention" -> "멘션"
        "author" -> "작성자"
        "comment" -> "댓글"
        "ci_activity" -> "CI"
        "subscribed" -> "구독"
        else -> notification.reason
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.unread)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            if (notification.unread) GitHubBlue.copy(alpha = 0.4f) else GitHubBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (notification.unread) {
                Box(
                    modifier = Modifier.padding(top = 4.dp).size(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = GitHubBlue, modifier = Modifier.size(8.dp)) {}
                }
            } else {
                Spacer(modifier = Modifier.size(8.dp))
            }

            Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(18.dp).padding(top = 2.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.repository.fullName,
                    fontSize = 11.sp,
                    color = GitHubTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.subject.title,
                    fontSize = 13.sp,
                    fontWeight = if (notification.unread) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReasonBadge(label = reasonLabel)
                    Text(
                        text = formatNotifDate(notification.updatedAt),
                        fontSize = 11.sp,
                        color = GitHubTextSecondary
                    )
                }
            }

            if (notification.unread) {
                IconButton(onClick = onMarkRead, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Check, contentDescription = "읽음 처리", tint = GitHubTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ReasonBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = GitHubBorder
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = GitHubTextSecondary,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

private fun formatNotifDate(dateStr: String): String {
    return try {
        val instant = Instant.parse(dateStr)
        val now = Instant.now()
        val hours = ChronoUnit.HOURS.between(instant, now)
        when {
            hours < 1 -> "${ChronoUnit.MINUTES.between(instant, now)}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MM/dd").withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    } catch (e: Exception) {
        dateStr.take(10)
    }
}
