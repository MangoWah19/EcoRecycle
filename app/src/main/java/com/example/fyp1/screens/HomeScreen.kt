package com.example.fyp1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.MainViewModel
import com.example.fyp1.RecyclingLog
import com.example.fyp1.components.BottomNavigationBar

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7F5))
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HomeTopBar(
                    isRefreshing = viewModel.isRefreshing,
                    onRefresh = { viewModel.fetchUserData() },
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            item {
                ImpactPanel(
                    userName = viewModel.userName,
                    userPoints = viewModel.userPoints
                )
            }

            item {
                SectionLabel("Get Started")
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionTile(
                        label = "Scan",
                        icon = Icons.Default.Recycling,
                        backgroundColor = Color(0xFFEFF8F0),
                        iconColor = Color(0xFF006B1B),
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("submit_recycling") }
                    HomeActionTile(
                        label = "Rewards",
                        icon = Icons.Default.CardGiftcard,
                        backgroundColor = Color(0xFFF0FFF4),
                        iconColor = Color(0xFF006A38),
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("rewards") }
                    HomeActionTile(
                        label = "Achievements",
                        icon = Icons.Default.EmojiEvents,
                        backgroundColor = Color(0xFFF0FBFF),
                        iconColor = Color(0xFF00656F),
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("achievements") }
                }
                Spacer(Modifier.height(12.dp))
                LeaderboardCard { navController.navigate("leaderboard") }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Recent History")
                    TextButton(onClick = { navController.navigate("recycling_history") }) {
                        Text(
                            text = "VIEW ALL",
                            color = Color(0xFF006B1B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            if (viewModel.recyclingHistory.isEmpty() && !viewModel.isRefreshing) {
                item { EmptyHistoryCard() }
            }

            items(viewModel.recyclingHistory.take(3)) { log ->
                RecentHistoryCard(log)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun HomeTopBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRefresh) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF006B1B),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF006B1B))
                }
            }
            Text(
                text = "Eco-Recycle",
                color = Color(0xFF006B1B),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable { onProfileClick() },
            shape = CircleShape,
            color = Color(0xFFE6E9E7),
            border = BorderStroke(2.dp, Color(0x1A006B1B))
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color(0xFF006B1B),
                modifier = Modifier.padding(9.dp)
            )
        }
    }
}

@Composable
private fun ImpactPanel(userName: String, userPoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp)) {
            Text(
                text = userName.ifBlank { "Eco Recycler" },
                color = Color(0xFF2C2F2E),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Personal sustainability report",
                color = Color(0xFF595C5B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$userPoints",
                        color = Color(0xFF006B1B),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "ECO POINTS",
                        color = Color(0xFF595C5B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0x1A006B1B)
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color(0xFF006B1B),
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0x99595C5B),
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.5.sp
    )
}

@Composable
private fun HomeActionTile(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(118.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.10f)
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(9.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = label.uppercase(),
                color = Color(0xCC2C2F2E),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LeaderboardCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1EF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x1A006B1B)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF006B1B),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "COMMUNITY LEADERBOARD",
                    color = Color(0xCC2C2F2E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
            Text(text = ">", color = Color(0xFF595C5B), fontSize = 22.sp)
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "No logs yet. Start recycling!",
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            color = Color(0xFF595C5B),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentHistoryCard(log: RecyclingLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0x1AABAEAC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0x1A006B1B)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF006B1B),
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (log.status == "Approved") "Deposit approved" else "Deposit submitted",
                    color = Color(0xFF2C2F2E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${log.material_type} - ${log.quantity}kg",
                    color = Color(0xFF595C5B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0x0D006B1B)
            ) {
                Text(
                    text = if (log.status == "Approved") "+${log.points_awarded} pts" else log.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color(0xFF006B1B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}