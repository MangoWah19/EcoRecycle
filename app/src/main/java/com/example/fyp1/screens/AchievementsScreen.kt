package com.example.fyp1.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    // Build achievement list with live progress values from ViewModel
    val allAchievements = listOf(
        AchievementBadge(
            type        = "plastic_king",
            title       = "Plastic King \uD83D\uDC51",
            description = "Recycle 100kg of plastic",
            icon        = "\uD83D\uDC51",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "plastic_king" },
            current     = viewModel.plasticKg.toDouble(),
            target      = 100.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "paper_master",
            title       = "Paper Master \uD83D\uDCDA",
            description = "Recycle 50kg of paper",
            icon        = "\uD83D\uDCDA",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "paper_master" },
            current     = viewModel.paperKg.toDouble(),
            target      = 50.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "glass_guard",
            title       = "Glass Guard \uD83D\uDD37",
            description = "Recycle 75kg of glass",
            icon        = "\uD83D\uDD37",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "glass_guard" },
            current     = viewModel.glassKg.toDouble(),
            target      = 75.0,
            unit        = "kg"
        ),
        AchievementBadge(
            type        = "eco_warrior",
            title       = "Eco Warrior \uD83C\uDF31",
            description = "Earn 1000 lifetime points",
            icon        = "\uD83C\uDF31",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "eco_warrior" },
            current     = viewModel.lifetimePoints.toDouble(),
            target      = 1000.0,
            unit        = "pts"
        ),
        AchievementBadge(
            type        = "week_streak",
            title       = "Week Streak \uD83D\uDD25",
            description = "Submit logs on 7 different days",
            icon        = "\uD83D\uDD25",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "week_streak" },
            current     = viewModel.streakDays.toDouble(),
            target      = 7.0,
            unit        = "days"
        ),
        AchievementBadge(
            type        = "first_redemption",
            title       = "First Redemption \uD83C\uDF81",
            description = "Redeem your first reward",
            icon        = "\uD83C\uDF81",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "first_redemption" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(1.0),
            target      = 1.0,
            unit        = "redemption"
        ),
        AchievementBadge(
            type        = "reward_collector",
            title       = "Reward Collector \uD83C\uDFC6",
            description = "Redeem 10 rewards total",
            icon        = "\uD83C\uDFC6",
            isUnlocked  = viewModel.userAchievements.any { it.achievement_type == "reward_collector" },
            current     = viewModel.totalRedemptions.toDouble().coerceAtMost(10.0),
            target      = 10.0,
            unit        = "redemptions"
        )
    )

    val unlockedCount = allAchievements.count { it.isUnlocked }
    val totalCount    = allAchievements.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1DB954)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FCF9))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary header
            item {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1DB954)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$unlockedCount / $totalCount Unlocked",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = Color(0xFFFFD700),
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${totalCount - unlockedCount} more to go!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(allAchievements) { achievement ->
                AchievementCard(achievement)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

