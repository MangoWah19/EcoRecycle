package com.example.fyp1

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
import com.example.fyp1.engines.*
import com.example.fyp1.navigation.AppNavigation


// ============================================
// CONSTANTS
// ============================================
const val DUPLICATE_DETECTION_WINDOW = 300 // 5 minutes in seconds
const val TOP_N_ENTRIES = 10
const val DEFAULT_CLAIM_EXPIRY_DAYS = 30
const val MIN_SUBMISSION_INTERVAL = 60 // 1 minute in seconds
const val MAX_DAILY_POINTS = 1000
const val MAX_SUBMISSIONS_PER_HOUR = 10


val POINT_RATES = mapOf("Plastic" to 50, "Paper" to 20, "Glass" to 30, "Metal" to 60)
const val MIN_ACTIVITY_LOGS = 3  // Minimum approved submissions to appear on leaderboard

// ============================================
// SUPABASE INITIALIZATION
// ============================================
val supabase = createSupabaseClient(
    supabaseUrl = "https://neoqwwguannvgsrlxsap.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5lb3F3d2d1YW5udmdzcmx4c2FwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkxNzYxMjcsImV4cCI6MjA4NDc1MjEyN30._jUajSswkd57rOks0aWwfZSamHVnTWiGJG_CjPnmTbw"
) {
    install(Postgrest)
    install(Auth)
}

val antiGamingEngine = AntiGamingEngine(supabase)
val leaderboardEngine = LeaderboardEngine(supabase)
val rewardsEngine = RewardsEngine(supabase)
val pointsLedger = PointsLedger(supabase)


// ============================================
// ANTI-GAMING ENGINE
// ============================================

// ============================================
// VIEWMODEL
// ============================================

class MainViewModel : ViewModel() {
    var userPoints by mutableIntStateOf(0)
    var userName by mutableStateOf("Loading...")
    var isRefreshing by mutableStateOf(false)

    val leaderboard = mutableStateListOf<LeaderboardEntry>()
    val leaderboardWithRank = mutableStateListOf<LeaderboardEntryWithRank>()
    var isLoadingLeaderboard by mutableStateOf(false)
    var currentLeaderboardTimeframe by mutableStateOf("all_time")

    val rewardsCatalog = mutableStateListOf<Reward>()
    val redemptionHistory = mutableStateListOf<Redemption>()
    val recyclingHistory = mutableStateListOf<RecyclingLog>()

    val userAchievements = mutableStateListOf<Achievement>()
    var isLoadingAchievements by mutableStateOf(false)

    // Achievement progress tracking 闂?updated in fetchUserData()
    var lifetimePoints by mutableIntStateOf(0)
    var plasticKg by mutableFloatStateOf(0f)
    var paperKg  by mutableFloatStateOf(0f)
    var glassKg  by mutableFloatStateOf(0f)
    var metalKg  by mutableFloatStateOf(0f)
    var streakDays by mutableIntStateOf(0)
    var totalRedemptions by mutableIntStateOf(0)

    fun fetchUserData() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) {
                    userName = "Not Logged In"
                    userPoints = 0
                    return@launch
                }

                try {
                    val profile = supabase.postgrest["profiles"]
                        .select { filter { eq("id", user.id) } }
                        .decodeSingle<Profile>()
                    userName = profile.full_name ?: profile.username ?: user.email?.substringBefore("@") ?: "User"
                    userPoints = profile.total_points
                } catch (e: Exception) {
                    userName = user.email?.substringBefore("@") ?: "User"
                }

                try {
                    val rewards = supabase.postgrest["rewards_catalog"]
                        .select { filter { eq("is_active", true) } }
                        .decodeList<Reward>()
                    rewardsCatalog.clear()
                    rewardsCatalog.addAll(rewards)
                    android.util.Log.d("REWARDS", "Loaded ${rewards.size} rewards")
                    rewards.forEach { android.util.Log.d("REWARDS", "Reward: ${it.name} | Image: ${it.image_url}") }
                } catch (e: Exception) {
                    android.util.Log.e("REWARDS", "Error loading rewards: ${e.message}")
                }

                try {
                    val history = supabase.postgrest["redemptions"]
                        .select { filter { eq("user_id", user.id) } }
                        .decodeList<Redemption>()
                    redemptionHistory.clear()
                    redemptionHistory.addAll(history.reversed())
                } catch (e: Exception) { }

                try {
                    val logs = supabase.postgrest["recycling_logs"]
                        .select {
                            filter { eq("user_id", user.id) }
                            order(column = "created_at", order = Order.DESCENDING)
                        }
                        .decodeList<RecyclingLog>()
                    recyclingHistory.clear()
                    recyclingHistory.addAll(logs)
                } catch (e: Exception) { }

                try {
                    val achievements = supabase.postgrest["achievement_unlocks"]
                        .select { filter { eq("user_id", user.id) } }
                        .decodeList<Achievement>()
                    userAchievements.clear()
                    userAchievements.addAll(achievements)
                } catch (e: Exception) { }

                // Fetch achievement progress stats
                try {
                    val profile = supabase.postgrest["profiles"]
                        .select { filter { eq("id", user.id) } }
                        .decodeSingle<Profile>()
                    lifetimePoints = profile.lifetime_points

                    val approvedLogs = supabase.postgrest["recycling_logs"]
                        .select { filter { eq("user_id", user.id); eq("status", "Approved") } }
                        .decodeList<RecyclingLog>()

                    plasticKg = approvedLogs.filter { it.material_type == "Plastic" }.sumOf { it.quantity }.toFloat()
                    paperKg   = approvedLogs.filter { it.material_type == "Paper"   }.sumOf { it.quantity }.toFloat()
                    glassKg   = approvedLogs.filter { it.material_type == "Glass"   }.sumOf { it.quantity }.toFloat()
                    metalKg   = approvedLogs.filter { it.material_type == "Metal"   }.sumOf { it.quantity }.toFloat()

                    val sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 3600).toString()
                    val recentLogs = approvedLogs.filter {
                        it.created_at != null && it.created_at >= sevenDaysAgo
                    }
                    streakDays = recentLogs.mapNotNull { it.created_at?.take(10) }.toSet().size

                    val redemptions = supabase.postgrest["redemptions"]
                        .select { filter { eq("user_id", user.id) } }
                        .decodeList<Redemption>()
                    totalRedemptions = redemptions.size
                } catch (e: Exception) { }

            } catch (e: Exception) {
                userName = "Error Loading"
            } finally {
                isRefreshing = false
            }
        }
    }

    /**
     * Fetch leaderboard for a specific timeframe: "daily", "weekly", or "all_time".
     * Routes to the correct algorithm in LeaderboardEngine including anti-gaming gate.
     */
    fun fetchLeaderboard(timeframe: String = "all_time") {
        viewModelScope.launch {
            isLoadingLeaderboard = true
            currentLeaderboardTimeframe = timeframe
            try {
                val response = leaderboardEngine.getLeaderboard(timeframe, includeRankChange = true)
                leaderboardWithRank.clear()
                leaderboardWithRank.addAll(response.entries)

                // Also populate legacy leaderboard list for any existing code using it
                leaderboard.clear()
                leaderboard.addAll(response.entries.map { entry ->
                    LeaderboardEntry(
                        full_name = entry.full_name,
                        lifetime_points = entry.lifetime_points,
                        total_points = entry.total_points,
                        role = "user",
                        id = entry.user_id
                    )
                })
            } catch (e: Exception) {
                // Handle error silently
            } finally {
                isLoadingLeaderboard = false
            }
        }
    }

    fun updatePointsLocal(newAmount: Int) {
        userPoints = newAmount
    }

    fun redeemItem(reward: Reward, quantity: Int = 1, context: android.content.Context) {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull() ?: return@launch

            // Create a virtual reward with points multiplied by quantity for eligibility check
            val scaledReward = reward.copy(points_required = reward.points_required * quantity)

            val eligibility = rewardsEngine.checkEligibility(user.id, scaledReward)
            if (!eligibility.isEligible) {
                Toast.makeText(context, "Cannot Redeem: ${eligibility.reasons.firstOrNull()}", Toast.LENGTH_LONG).show()
                return@launch
            }

            val cooldownInfo = antiGamingEngine.checkRedemptionCooldown(user.id, reward.id ?: 0)
            if (!cooldownInfo.canRedeem) {
                Toast.makeText(context, cooldownInfo.cooldownReason, Toast.LENGTH_LONG).show()
                return@launch
            }

            rewardsEngine.expireStaleRedemptions(user.id)

            try {
                val expiryDate = Instant.now()
                    .plusSeconds((DEFAULT_CLAIM_EXPIRY_DAYS * 24 * 3600).toLong())
                    .toString()

                // Insert one redemption row per quantity
                repeat(quantity) {
                    val newRedemption = Redemption(
                        user_id = user.id,
                        reward_id = reward.id,
                        item_name = reward.name,
                        points_spent = reward.points_required,
                        status = "claimed",
                        claimed_at = Instant.now().toString(),
                        expires_at = expiryDate
                    )
                    supabase.postgrest["redemptions"].insert(newRedemption)
                }

                // Deduct total points
                val spent = pointsLedger.spendPoints(
                    user.id,
                    reward.points_required * quantity,
                    "Reward redemption x$quantity: ${reward.name}"
                )
                if (!spent) {
                    Toast.makeText(context, "Point deduction failed. Please check your balance.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Decrease stock by quantity
                supabase.postgrest.rpc(
                    "decrement_reward_stock",
                    buildJsonObject {
                        put("p_reward_id", reward.id!!)
                        put("p_quantity", quantity)
                    }
                )

                antiGamingEngine.recordCooldownAfterRedemption(user.id, reward.id ?: 0)
                rewardsEngine.checkAndUnlockRedemptionAchievements(user.id)
                fetchUserData()

                Toast.makeText(
                    context,
                    "Redeemed $quantity x \"${reward.name}\" (${reward.points_required * quantity} pts). Expires in $DEFAULT_CLAIM_EXPIRY_DAYS days.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Redemption Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun submitRecyclingLog(materialType: String, quantity: Double, context: android.content.Context) {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull() ?: return@launch

            val validation = antiGamingEngine.validateRecyclingSubmission(user.id, materialType, quantity)

            if (!validation.isValid) {
                Toast.makeText(
                    context,
                    "Submission Blocked: ${validation.issues.firstOrNull() ?: "Unknown error. Please try again."}",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            try {
                supabase.postgrest["profiles"].update(
                    mapOf("last_log_submission_at" to Instant.now().toString())
                ) {
                    filter { eq("id", user.id) }
                }

                val newLog = RecyclingLog(
                    user_id = user.id,
                    material_type = materialType,
                    quantity = quantity,
                    status = "Pending",
                    points_awarded = 0
                )
                supabase.postgrest["recycling_logs"].insert(newLog)

                // Check and unlock recycling-based achievements after each submission
                antiGamingEngine.checkAndUnlockRecyclingAchievements(user.id)

                // Take leaderboard snapshot daily (silently)
                leaderboardEngine.createLeaderboardSnapshot()

                Toast.makeText(
                    context,
                    "Submitted Successfully! Your ${quantity}kg of $materialType is now pending admin approval. Points will be awarded once approved.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Submission Failed: Could not save your recycling log. Please check your internet connection and try again. (${e.localizedMessage})",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

// ============================================
// MAIN ACTIVITY
// ============================================


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FYP1Theme {
                AppNavigation(this@MainActivity, intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
