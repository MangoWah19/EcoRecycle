package com.example.fyp1.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp1.AchievementBadge
import com.example.fyp1.MainViewModel
import com.example.fyp1.R
import com.example.fyp1.components.FloatingBottomNavigationScaffold

private val MissionTabs = listOf("All", "New", "Ongoing", "Pending", "Pending Review", "Completed")

private enum class MissionUiStatus {
    New,
    Ongoing,
    Pending,
    PendingReview,
    Approved,
    Rejected,
    Completed
}

@Composable
fun MissionsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    val allAchievements = missionAchievements(viewModel)
    val missionAchievements = allAchievements.sortedBy { if (it.type == "eco_warrior") 0 else 1 }
    var selectedMissionTab by remember { mutableStateOf("All") }
    val missionStatusOverrides = remember { mutableStateMapOf<String, MissionUiStatus>() }
    val visibleMissions = missionAchievements.filter { achievement ->
        val status = missionStatusOverrides[achievement.type] ?: missionStatusFor(achievement)
        missionMatchesTab(status, selectedMissionTab)
    }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MissionBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { MissionTopBar(onMenuClick = { }, onProfileClick = { navController.navigate("profile") }) }
            item { MissionHeader() }
            item {
                MissionSearchAndFilters(
                    selectedTab = selectedMissionTab,
                    onTabSelected = { selectedMissionTab = it }
                )
            }
            if (selectedMissionTab == "All" || selectedMissionTab == "New") {
                item { CoffeeMissionBrowseCard(onClick = { navController.navigate("mission_details/zero_waste_coffee") }) }
            }
            items(visibleMissions) { achievement ->
                MissionAchievementCard(
                    achievement = achievement,
                    featured = achievement.type == "eco_warrior",
                    status = missionStatusOverrides[achievement.type] ?: missionStatusFor(achievement),
                    onClick = {
                        if (achievement.type == "plastic_king") {
                            navController.navigate("mission_details/plastic_king")
                        } else {
                            val status = missionStatusOverrides[achievement.type] ?: missionStatusFor(achievement)
                            if (status == MissionUiStatus.PendingReview) {
                                missionStatusOverrides[achievement.type] = if (achievement.isUnlocked || missionProgress(achievement) >= 1f) {
                                    MissionUiStatus.Completed
                                } else {
                                    MissionUiStatus.Ongoing
                                }
                            }
                            Toast.makeText(context, "Mission details coming soon", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

private fun missionAchievements(viewModel: MainViewModel): List<AchievementBadge> = listOf(
    AchievementBadge("plastic_king", "Plastic King", "Recycle 100kg of plastic", "?", viewModel.userAchievements.any { it.achievement_type == "plastic_king" }, viewModel.plasticKg.toDouble(), 100.0, "kg"),
    AchievementBadge("paper_master", "Paper Master", "Recycle 50kg of paper", "?", viewModel.userAchievements.any { it.achievement_type == "paper_master" }, viewModel.paperKg.toDouble(), 50.0, "kg"),
    AchievementBadge("glass_guard", "Glass Guard", "Recycle 75kg of glass", "?", viewModel.userAchievements.any { it.achievement_type == "glass_guard" }, viewModel.glassKg.toDouble(), 75.0, "kg"),
    AchievementBadge("eco_warrior", "Zero-Waste Campus Week", "Earn 1000 lifetime points by joining campus recycling activities.", "??", viewModel.userAchievements.any { it.achievement_type == "eco_warrior" }, viewModel.lifetimePoints.toDouble(), 1000.0, "pts"),
    AchievementBadge("week_streak", "Weekly Recycling Streak", "Submit recycling logs on 7 different days.", "??", viewModel.userAchievements.any { it.achievement_type == "week_streak" }, viewModel.streakDays.toDouble(), 7.0, "days"),
    AchievementBadge("first_redemption", "First Reward Claim", "Redeem your first reward from the reward hub.", "??", viewModel.userAchievements.any { it.achievement_type == "first_redemption" }, viewModel.totalRedemptions.toDouble().coerceAtMost(1.0), 1.0, "redemption"),
    AchievementBadge("reward_collector", "Reward Collector", "Redeem 10 rewards total.", "??", viewModel.userAchievements.any { it.achievement_type == "reward_collector" }, viewModel.totalRedemptions.toDouble().coerceAtMost(10.0), 10.0, "redemptions")
)

@Composable
private fun CoffeeMissionBrowseCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE5EAE6))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.mission_zero_waste_coffee),
                    contentDescription = "Zero-Waste Coffee Day",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.48f))))
                )
                Text(
                    text = "Zero-Waste Coffee Day",
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 15.dp),
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    MissionStatusLabel(MissionUiStatus.New, "COFFEE WASTE")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("150 pts", color = MissionPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Bring your reusable cup and skip the disposable cup during a campus coffee run.", color = MissionMuted, fontSize = 13.sp, lineHeight = 19.sp)
                MissionButton(text = "View Details", primary = true, enabled = true, onClick = onClick)
            }
        }
    }
}

@Composable
private fun MissionTopBar(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MissionPrimary)
            }
            Text("Eco-Recycle", color = MissionPrimary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            modifier = Modifier.size(42.dp).clickable(onClick = onProfileClick),
            shape = CircleShape,
            color = Color(0xFFE6E9E7),
            border = BorderStroke(2.dp, Color(0x1A006B1B))
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MissionPrimary, modifier = Modifier.padding(9.dp))
        }
    }
}

@Composable
private fun MissionHeader() {
    Column(modifier = Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Active Missions", color = MissionPrimary, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
        Text("Join the green revolution on campus.", color = MissionMuted, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MissionSearchAndFilters(selectedTab: String, onTabSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp), color = MissionSoftSurface) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF747776), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Search sustainability tasks...", color = Color(0xFF747776), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF747776), modifier = Modifier.size(19.dp))
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MissionTabs.forEach { tab ->
                MissionFilterChip(label = tab, selected = selectedTab == tab, onClick = { onTabSelected(tab) })
            }
        }
    }
}

@Composable
private fun MissionFilterChip(label: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = CircleShape, color = if (selected) MissionPrimary else MissionSoftSurface) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = if (selected) Color.White else MissionMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MissionAchievementCard(achievement: AchievementBadge, featured: Boolean, status: MissionUiStatus, onClick: () -> Unit) {
    val progress = missionProgress(achievement)
    val completed = status == MissionUiStatus.Completed || achievement.isUnlocked || progress >= 1f
    val started = status == MissionUiStatus.Ongoing
    val points = missionPoints(achievement.type)
    val category = missionCategory(achievement.type)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = when {
            status == MissionUiStatus.PendingReview -> BorderStroke(2.dp, Color(0xFF11D5E8))
            status == MissionUiStatus.Approved -> BorderStroke(2.dp, Color(0xFF86FAAC))
            status == MissionUiStatus.Rejected -> BorderStroke(2.dp, Color(0xFFFF8A80))
            started -> BorderStroke(2.dp, MissionAccent)
            featured -> null
            else -> BorderStroke(1.dp, Color(0xFFE5EAE6))
        }
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                MissionStatusLabel(status, category)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$points pts", color = MissionPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(achievement.title, color = MissionText, fontSize = 19.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text(missionDescription(achievement), color = MissionMuted, fontSize = 13.sp, lineHeight = 19.sp)
            if (status == MissionUiStatus.PendingReview) MissionReviewNote()
            if (started || status == MissionUiStatus.PendingReview) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = MissionAccent,
                    trackColor = MissionSoftSurface
                )
            }
            MissionButton(
                text = when {
                    status == MissionUiStatus.PendingReview -> "Check Status"
                    status == MissionUiStatus.Approved -> "Details"
                    status == MissionUiStatus.Rejected -> "View Feedback"
                    completed -> "Completed"
                    started -> "View Progress"
                    status == MissionUiStatus.Pending -> "Pending"
                    else -> "View Details"
                },
                primary = featured || started || status == MissionUiStatus.PendingReview,
                enabled = status != MissionUiStatus.Pending && !completed,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun MissionStatusLabel(status: MissionUiStatus, fallbackCategory: String) {
    val label = when (status) {
        MissionUiStatus.New -> fallbackCategory
        MissionUiStatus.Ongoing -> "ONGOING"
        MissionUiStatus.Pending -> "PENDING"
        MissionUiStatus.PendingReview -> "PENDING REVIEW"
        MissionUiStatus.Approved -> "APPROVED"
        MissionUiStatus.Rejected -> "REJECTED"
        MissionUiStatus.Completed -> "COMPLETED"
    }
    val color = when (status) {
        MissionUiStatus.PendingReview -> Color(0xFFB9F6FF)
        MissionUiStatus.Pending -> Color(0xFFFFF1C2)
        MissionUiStatus.Approved, MissionUiStatus.Completed -> Color(0xFFC7FFD6)
        MissionUiStatus.Rejected -> Color(0xFFFFDAD6)
        else -> Color(0xFFE2F6E6)
    }
    val textColor = when (status) {
        MissionUiStatus.PendingReview -> Color(0xFF007C8A)
        MissionUiStatus.Pending -> Color(0xFF9B6A00)
        MissionUiStatus.Rejected -> Color(0xFFB3261E)
        else -> MissionPrimary
    }
    Surface(shape = CircleShape, color = color) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp)
    }
}

@Composable
private fun MissionReviewNote() {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEAF6F7)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFF007C8A)) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("Verification photo uploaded. Review typically takes 24-48 hours.", color = MissionMuted, fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MissionButton(text: String, primary: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = when {
            !enabled -> MissionSoftSurface
            primary -> MissionPrimary
            else -> MissionSoftSurface
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = if (primary && enabled) Color.White else MissionPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private val MissionBackground = Color(0xFFF5F7F5)
private val MissionPrimary = Color(0xFF006B1B)
private val MissionAccent = Color(0xFF008A95)
private val MissionText = Color(0xFF2C2F2E)
private val MissionMuted = Color(0xFF686E6B)
private val MissionSoftSurface = Color(0xFFE6EDE9)

private fun missionProgress(achievement: AchievementBadge): Float = if (achievement.target > 0) {
    (achievement.current / achievement.target).toFloat().coerceIn(0f, 1f)
} else {
    0f
}

private fun missionStatusFor(achievement: AchievementBadge): MissionUiStatus {
    val progress = missionProgress(achievement)
    return when {
        achievement.isUnlocked || progress >= 1f -> MissionUiStatus.Completed
        progress >= 0.65f -> MissionUiStatus.PendingReview
        achievement.current > 0.0 -> MissionUiStatus.Ongoing
        else -> MissionUiStatus.New
    }
}

private fun missionMatchesTab(status: MissionUiStatus, tab: String): Boolean = when (tab) {
    "All" -> true
    "New" -> status == MissionUiStatus.New
    "Ongoing" -> status == MissionUiStatus.Ongoing
    "Pending" -> status == MissionUiStatus.Pending
    "Pending Review" -> status == MissionUiStatus.PendingReview || status == MissionUiStatus.Approved || status == MissionUiStatus.Rejected
    "Completed" -> status == MissionUiStatus.Completed
    else -> true
}

private fun missionPoints(type: String): Int = when (type) {
    "eco_warrior" -> 200
    "plastic_king" -> 120
    "paper_master" -> 90
    "glass_guard" -> 100
    "week_streak" -> 80
    "first_redemption" -> 50
    "reward_collector" -> 150
    else -> 75
}

private fun missionCategory(type: String): String = when (type) {
    "eco_warrior" -> "WASTE MANAGEMENT"
    "plastic_king" -> "RECYCLING"
    "paper_master" -> "PAPER"
    "glass_guard" -> "GLASS"
    "week_streak" -> "MOBILITY"
    "first_redemption" -> "REWARDS"
    "reward_collector" -> "COMMUNITY"
    else -> "MISSION"
}

private fun missionDescription(achievement: AchievementBadge): String = when (achievement.type) {
    "eco_warrior" -> "Coordinate with your dorm floor to eliminate single-use plastics for 7 days. Track your collective progress."
    "plastic_king" -> "Collect and submit clean plastic bottles or containers to move this recycling mission forward."
    "paper_master" -> "Sort clean paper, flatten cardboard, and keep materials dry before depositing."
    "glass_guard" -> "Rinse glass bottles and jars before sending them to the campus recycling stream."
    "week_streak" -> "Keep your recycling habit alive by submitting approved activity across different days."
    "first_redemption" -> "Turn your earned points into your first campus reward."
    "reward_collector" -> "Keep redeeming rewards and build your sustainable campus streak."
    else -> achievement.description
}