package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.api.NotificationRepository
import com.example.fyp1.components.FloatingBottomNavigationScaffold
import com.example.fyp1.offline.LocalNotificationEntity
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

private val NotificationBackground = Color(0xFFF5F7F5)
private val NotificationPrimary = Color(0xFF006B1B)
private val NotificationText = Color(0xFF2C2F2E)
private val NotificationMuted = Color(0xFF646B67)

private enum class NotificationTab(val label: String, val category: String? = null, val unreadOnly: Boolean = false) {
    All("All"),
    Unread("Unread", unreadOnly = true),
    Missions("Missions", "MISSION"),
    Recycling("Recycling", "RECYCLING"),
    Rewards("Rewards", "REWARD"),
    Leaderboard("Leaderboard", "LEADERBOARD")
}

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { NotificationRepository(context) }
    val notifications by repository.observeNotifications().collectAsState(initial = emptyList())
    val unreadCount by repository.observeUnreadCount().collectAsState(initial = 0)
    val scope = rememberCoroutineScope()
    val tabs = remember { NotificationTab.entries.toList() }
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeTab = tabs[selectedTab]
    val filteredNotifications = notifications.filter { notification ->
        val unreadMatch = !activeTab.unreadOnly || !notification.isRead
        val categoryMatch = activeTab.category == null || notification.category == activeTab.category
        unreadMatch && categoryMatch
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NotificationBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                NotificationTopBar(
                    unreadCount = unreadCount,
                    onBack = { navController.popBackStack() },
                    onMarkAllRead = {
                        scope.launch { repository.markAllAsRead() }
                    }
                )
            }
            item {
                NotificationSummaryCard(unreadCount = unreadCount, totalCount = notifications.size)
            }
            item {
                NotificationTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent",
                        color = NotificationText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "Mark all as read",
                            color = NotificationPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                scope.launch { repository.markAllAsRead() }
                            }
                        )
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    NotificationEmptyState(activeTab)
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            if (!notification.isRead) {
                                scope.launch { repository.markAsRead(notification.id) }
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NotificationPrimary)
            }
            Text(
                text = "Notifications",
                color = NotificationPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (unreadCount > 0) {
            Text(
                text = "Mark all read",
                color = NotificationPrimary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMarkAllRead
                )
            )
        }
    }
}

@Composable
private fun NotificationSummaryCard(unreadCount: Int, totalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = NotificationPrimary.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .size(82.dp)
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = CircleShape, color = Color(0xFFE3F7E7)) {
                    Text(
                        text = "ACTIVITY HUB",
                        color = NotificationPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = if (unreadCount > 0) "$unreadCount Unread Update${if (unreadCount == 1) "" else "s"}" else "You're all caught up",
                    color = NotificationText,
                    fontSize = 25.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (totalCount > 0) {
                        "Stay updated with your missions, recycling deposits, rewards, badges, and leaderboard rank."
                    } else {
                        "Updates about your eco-impact will appear here once activity changes."
                    },
                    color = NotificationMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NotificationTabs(
    tabs: List<NotificationTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Surface(
                modifier = Modifier
                    .height(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelected(index) },
                shape = CircleShape,
                color = if (selected) NotificationPrimary else Color(0xFFE6E9E7),
                shadowElevation = if (selected) 3.dp else 0.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = if (selected) Color.White else Color(0xFF686D6A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: LocalNotificationEntity,
    onClick: () -> Unit
) {
    val style = notification.notificationStyle()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 3.dp),
        border = BorderStroke(1.dp, if (notification.isRead) Color(0xFFE8EBE9) else style.color.copy(alpha = 0.22f))
    ) {
        Box {
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(5.dp)
                        .height(56.dp)
                        .background(style.color, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (notification.isRead) Color(0xFFE2E5E3) else style.color.copy(alpha = 0.20f)
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = if (notification.isRead) Color(0xFF777C79) else style.color,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            color = if (notification.isRead) NotificationText.copy(alpha = 0.72f) else NotificationText,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = notification.createdAt.timeAgo(),
                            color = NotificationMuted.copy(alpha = 0.82f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = notification.message,
                        color = NotificationMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(shape = CircleShape, color = Color(0xFFE7EAE8)) {
                        Text(
                            text = style.label,
                            color = Color(0xFF6B706D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyState(tab: NotificationTab) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(62.dp), shape = CircleShape, color = Color(0xFFE9F4EB)) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = NotificationPrimary,
                    modifier = Modifier.padding(17.dp)
                )
            }
            Text(
                text = if (tab == NotificationTab.All) "No notifications yet" else "No ${tab.label.lowercase()} notifications",
                color = NotificationText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Updates about your missions, recycling deposits, rewards, and leaderboard will appear here.",
                color = NotificationMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

private data class NotificationStyle(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private fun LocalNotificationEntity.notificationStyle(): NotificationStyle {
    return when (category.uppercase()) {
        "MISSION" -> NotificationStyle("Mission", Icons.Default.EmojiEvents, Color(0xFF00656F))
        "RECYCLING" -> NotificationStyle("Recycling", Icons.Default.Recycling, NotificationPrimary)
        "REWARD" -> NotificationStyle("Rewards", Icons.Default.Redeem, Color(0xFF007A3D))
        "LEADERBOARD" -> NotificationStyle("Leaderboard", Icons.Default.Stars, Color(0xFF00A7B8))
        else -> NotificationStyle("Update", Icons.Default.CheckCircle, Color(0xFF5F6662))
    }
}

private fun Long.timeAgo(): String {
    val duration = runCatching { Duration.between(Instant.ofEpochMilli(this), Instant.now()) }.getOrNull()
        ?: return "just now"
    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} min ago"
        duration.toHours() < 24 -> "${duration.toHours()} hr ago"
        duration.toDays() < 7 -> "${duration.toDays()} day${if (duration.toDays() == 1L) "" else "s"} ago"
        else -> "${duration.toDays() / 7} wk ago"
    }
}
