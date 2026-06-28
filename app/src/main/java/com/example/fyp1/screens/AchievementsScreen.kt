package com.example.fyp1.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.fyp1.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.fyp1.ui.theme.FYP1Theme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.fyp1.*
import com.example.fyp1.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController, viewModel: MainViewModel) {
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    val allAchievements = listOf(
        AchievementBadge(
            type        = "plastic_king",
            title       = "Plastic King",
            description = "Recycle 100kg of plastic",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "plastic_king" },
            current     = viewModel.plasticKg.toDouble(),
            target      = 100.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "paper_master",
            title       = "Paper Master",
            description = "Recycle 50kg of paper",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "paper_master" },
            current     = viewModel.paperKg.toDouble(),
            target      = 50.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "glass_guard",
            title       = "Glass Guard",
            description = "Recycle 75kg of glass",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "glass_guard" },
            current     = viewModel.glassKg.toDouble(),
            target      = 75.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "eco_warrior",
            title       = "Eco Warrior",
            description = "Earn 1000 lifetime points",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "eco_warrior" },
            current     = viewModel.lifetimePoints.toDouble(),
            target      = 1000.0,
            unit        = "pts"
        ),
        AchievementBadge(
            type        = "week_streak",
            title       = "Week Streak",
            description = "Submit logs on 7 different days",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "week_streak" },
            current     = viewModel.streakDays.toDouble(),
            target      = 7.0,
            unit        = "days"
        ),
        AchievementBadge(
            type        = "first_redemption",
            title       = "First Redemption",
            description = "Redeem your first reward",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "first_redemption" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(1.0),
            target      = 1.0,
            unit        = "redemption"
        ),
        AchievementBadge(
            type        = "reward_collector",
            title       = "Reward Collector",
            description = "Redeem 10 rewards total",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "reward_collector" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(10.0),
            target      = 10.0,
            unit        = "redemptions"
        )
    )

    val unlockedCount = allAchievements.count { it.isUnlocked }
    val totalCount = allAchievements.size
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { AchievementRewardStyleHeader(onBack = { navController.popBackStack() }) }
            item {
                AchievementSummaryCard(
                    unlockedCount = unlockedCount,
                    totalCount = totalCount,
                    progress = progress
                )
            }
            items(allAchievements) { achievement ->
                StitchAchievementCard(achievement)
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun AchievementRewardStyleHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF006B1B)
            )
        }
        Text(
            text = "Achievements",
            color = Color(0xFF006B1B),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.fetchUserData() }

    // Keep mission progress backed by the existing achievement data.
    val allAchievements = listOf(
        AchievementBadge(
            type        = "plastic_king",
            title       = "Plastic King",
            description = "Recycle 100kg of plastic",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "plastic_king" },
            current     = viewModel.plasticKg.toDouble(),
            target      = 100.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "paper_master",
            title       = "Paper Master",
            description = "Recycle 50kg of paper",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "paper_master" },
            current     = viewModel.paperKg.toDouble(),
            target      = 50.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "glass_guard",
            title       = "Glass Guard",
            description = "Recycle 75kg of glass",
            icon        = "?",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "glass_guard" },
            current     = viewModel.glassKg.toDouble(),
            target      = 75.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "eco_warrior",
            title       = "Zero-Waste Campus Week",
            description = "Earn 1000 lifetime points by joining campus recycling activities.",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "eco_warrior" },
            current     = viewModel.lifetimePoints.toDouble(),
            target      = 1000.0,
            unit        = "pts"
        ),
        AchievementBadge(
            type        = "week_streak",
            title       = "Weekly Recycling Streak",
            description = "Submit recycling logs on 7 different days.",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "week_streak" },
            current     = viewModel.streakDays.toDouble(),
            target      = 7.0,
            unit        = "days"
        ),
        AchievementBadge(
            type        = "first_redemption",
            title       = "First Reward Claim",
            description = "Redeem your first reward from the reward hub.",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "first_redemption" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(1.0),
            target      = 1.0,
            unit        = "redemption"
        ),
        AchievementBadge(
            type        = "reward_collector",
            title       = "Reward Collector",
            description = "Redeem 10 rewards total.",
            icon        = "??",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "reward_collector" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(10.0),
            target      = 10.0,
            unit        = "redemptions"
        )
    )
    val missionAchievements = allAchievements.sortedBy { if (it.type == "eco_warrior") 0 else 1 }

    FloatingBottomNavigationScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MissionBackground)
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { MissionTopBar(onMenuClick = { }, onProfileClick = { navController.navigate("profile") }) }
            item { MissionHeader() }
            item { MissionSearchAndFilters() }
            items(missionAchievements) { achievement ->
                MissionAchievementCard(
                    achievement = achievement,
                    featured = achievement.type == "eco_warrior",
                    onClick = {
                        Toast.makeText(context, "Mission details coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

private val MissionBackground = Color(0xFFF5F7F5)
private val MissionPrimary = Color(0xFF006B1B)
private val MissionAccent = Color(0xFF008A95)
private val MissionText = Color(0xFF2C2F2E)
private val MissionMuted = Color(0xFF686E6B)
private val MissionSoftSurface = Color(0xFFE6EDE9)

@Composable
private fun MissionTopBar(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MissionPrimary)
            }
            Text(
                text = "Eco-Recycle",
                color = MissionPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onProfileClick),
            shape = CircleShape,
            color = Color(0xFFE6E9E7),
            border = BorderStroke(2.dp, Color(0x1A006B1B))
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = MissionPrimary,
                modifier = Modifier.padding(9.dp)
            )
        }
    }
}

@Composable
private fun MissionHeader() {
    Column(
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Active Missions",
            color = MissionPrimary,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Join the green revolution on campus.",
            color = MissionMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MissionSearchAndFilters() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                color = MissionSoftSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF747776), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Search sustainability tasks...",
                        color = Color(0xFF747776),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(8.dp),
                color = MissionSoftSurface
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = MissionMuted, modifier = Modifier.padding(14.dp))
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MissionFilterChip("All", selected = true)
            MissionFilterChip("New")
            MissionFilterChip("Ongoing")
            MissionFilterChip("Completed")
        }
    }
}

@Composable
private fun MissionFilterChip(label: String, selected: Boolean = false) {
    Surface(
        shape = CircleShape,
        color = if (selected) MissionPrimary else MissionSoftSurface
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            color = if (selected) Color.White else MissionMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MissionAchievementCard(
    achievement: AchievementBadge,
    featured: Boolean,
    onClick: () -> Unit
) {
    val progress = achievementProgress(achievement)
    val completed = achievement.isUnlocked || progress >= 1f
    val started = achievement.current > 0.0 && !completed
    val points = missionPoints(achievement.type)
    val category = missionCategory(achievement.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = when {
            started -> BorderStroke(2.dp, MissionAccent)
            featured -> null
            else -> BorderStroke(1.dp, Color(0xFFE5EAE6))
        }
    ) {
        Column {
            if (featured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mission_zero_waste),
                        contentDescription = achievement.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.04f), Color.Black.copy(alpha = 0.18f))
                                )
                            )
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        shape = CircleShape,
                        color = Color(0xFF11EAFE)
                    ) {
                        Text(
                            text = "FEATURED",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = Color(0xFF003D43),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MissionCategoryLabel(category)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MissionPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$points pts", color = MissionPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = achievement.title,
                    color = MissionText,
                    fontSize = if (featured) 22.sp else 17.sp,
                    lineHeight = if (featured) 27.sp else 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = missionDescription(achievement),
                    color = MissionMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                if (started) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp),
                        color = MissionAccent,
                        trackColor = MissionSoftSurface
                    )
                }
                MissionButton(
                    text = when {
                        completed -> "Completed"
                        started -> "Resume Mission"
                        else -> "View Details"
                    },
                    primary = featured || started,
                    enabled = !completed,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun MissionCategoryLabel(category: String) {
    Surface(shape = CircleShape, color = Color(0xFFE2F6E6)) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MissionPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun MissionButton(
    text: String,
    primary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = when {
            !enabled -> MissionSoftSurface
            primary -> MissionPrimary
            else -> MissionSoftSurface
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (primary && enabled) Color.White else MissionPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
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
@Composable
private fun AchievementSummaryCard(
    unlockedCount: Int,
    totalCount: Int,
    progress: Float
) {
    val percentage = (progress * 100).toInt()
    val remaining = (totalCount - unlockedCount).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00751D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "LIFETIME PROGRESS",
                        color = Color(0xCCD1FFC8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "$unlockedCount / $totalCount",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Unlocked",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.26f)
                ) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color(0xFFD1FFC8),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(12.dp),
                color = Color(0xFF86FAAC),
                trackColor = Color(0xFF005D16)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (remaining == 0) "All complete!" else "$remaining more to go!",
                    color = Color(0xFFD1FFC8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percentage%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun StitchAchievementCard(achievement: AchievementBadge) {
    val progress = achievementProgress(achievement)
    val percentage = (progress * 100).toInt()
    val remaining = (achievement.target - achievement.current).coerceAtLeast(0.0)
    val accent = achievementAccentColor(achievement.type)
    val completed = achievement.isUnlocked || progress >= 1f
    val container = if (completed) Color(0xFFD8FFE2) else Color.White
    val border = if (completed) Color(0xFF86FAAC) else Color(0xFFE6E9E7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = if (completed) 2.dp else 3.dp),
        border = BorderStroke(if (completed) 2.dp else 1.dp, border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = accent.copy(alpha = if (completed) 0.20f else 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = achievementIconVector(achievement.type),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = achievement.title,
                            color = if (completed) Color(0xFF006A38) else Color(0xFF2C2F2E),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = achievement.description,
                            color = Color(0xFF747776),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (completed) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = Color(0xFF006B1B)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (completed) 7.dp else 8.dp),
                color = if (completed) Color(0xFF006A38) else accent,
                trackColor = if (completed) Color(0x3386FAAC) else Color(0xFFE6E9E7)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (completed) "COMPLETED!" else "${formatAchievementNumber(achievement.current)} / ${formatAchievementNumber(achievement.target)} ${achievement.unit}",
                    color = if (completed) Color(0xFF006A38) else Color(0xFF747776),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (completed) 0.8.sp else 0.sp
                )

                if (!completed) {
                    Surface(
                        shape = CircleShape,
                        color = accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${formatAchievementNumber(remaining)} ${achievement.unit} left",
                            color = accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "$percentage%",
                    color = if (completed) Color(0xFF006A38) else Color(0xFF747776),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun achievementProgress(achievement: AchievementBadge): Float {
    return if (achievement.target > 0) {
        (achievement.current / achievement.target).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }
}


private fun achievementIconVector(type: String): ImageVector = when (type) {
    "plastic_king" -> Icons.Default.LocalDrink
    "paper_master" -> Icons.Default.Description
    "glass_guard" -> Icons.Default.WineBar
    "eco_warrior" -> Icons.Default.Eco
    "week_streak" -> Icons.Default.Schedule
    "first_redemption" -> Icons.Default.CardGiftcard
    "reward_collector" -> Icons.Default.EmojiEvents
    else -> Icons.Default.Stars
}
private fun achievementAccentColor(type: String): Color = when (type) {
    "plastic_king" -> Color(0xFF60A5FA)
    "paper_master" -> Color(0xFFFF8A3D)
    "glass_guard" -> Color(0xFF2DD4BF)
    "eco_warrior" -> Color(0xFF43A047)
    "week_streak" -> Color(0xFFFF7043)
    "first_redemption" -> Color(0xFF9C27B0)
    "reward_collector" -> Color(0xFFFFB300)
    else -> Color(0xFF006B1B)
}

private fun formatAchievementNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}








