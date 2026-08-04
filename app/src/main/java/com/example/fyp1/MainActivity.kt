package com.example.fyp1

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import java.io.ByteArrayOutputStream
import com.example.fyp1.api.AuthRepository
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.AuthUser
import com.example.fyp1.api.LeaderboardRepository
import com.example.fyp1.api.PointsRepository
import com.example.fyp1.api.RecyclingRepository
import com.example.fyp1.api.RewardRepository
import com.example.fyp1.components.AppPopOutMessage
import com.example.fyp1.components.PopOutMessageType
import com.example.fyp1.engines.*
import com.example.fyp1.navigation.AppNavigation
import com.example.fyp1.offline.isNetworkAvailable


// ============================================git
// CONSTANTS
// ============================================
const val DUPLICATE_DETECTION_WINDOW = 300 // 5 minutes in seconds
const val TOP_N_ENTRIES = 10
const val DEFAULT_CLAIM_EXPIRY_DAYS = 30
const val MIN_SUBMISSION_INTERVAL = 60 // 1 minute in seconds
const val MAX_DAILY_POINTS = 1000
const val MAX_SUBMISSIONS_PER_HOUR = 10
private const val HOME_CACHE_PREFS = "home_summary_cache"
private const val KEY_HOME_USER_NAME = "home_user_name"
private const val KEY_HOME_POINTS = "home_points"
private const val KEY_HOME_LIFETIME_POINTS = "home_lifetime_points"


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

private data class RecyclingProofPayload(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)

class MainViewModel : ViewModel() {
    var userPoints by mutableIntStateOf(0)
    var userName by mutableStateOf("Loading...")
    var isRefreshing by mutableStateOf(false)
    var backendUser by mutableStateOf<AuthUser?>(null)
    var popOutMessage by mutableStateOf<AppPopOutMessage?>(null)

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

    fun applyBackendUser(user: AuthUser) {
        backendUser = user
        userName = user.name
    }

    fun applyBackendPoints(points: Int, lifetime: Int = points) {
        userPoints = points
        lifetimePoints = lifetime
    }

    fun loadCachedHomeSummary(context: Context) {
        val appContext = context.applicationContext
        val cachedUser = AuthRepository(appContext).getSavedUser()
        if (cachedUser != null) {
            applyBackendUser(cachedUser)
        }

        val prefs = appContext.getSharedPreferences(HOME_CACHE_PREFS, Context.MODE_PRIVATE)
        val cachedName = prefs.getString(KEY_HOME_USER_NAME, null)
        if (!cachedName.isNullOrBlank()) {
            userName = cachedName
        }
        userPoints = prefs.getInt(KEY_HOME_POINTS, userPoints)
        lifetimePoints = prefs.getInt(KEY_HOME_LIFETIME_POINTS, lifetimePoints)
    }

    fun refreshBackendUserSummary(context: Context) {
        viewModelScope.launch {
            refreshBackendUserSummaryNow(context)
        }
    }

    fun refreshHomeProfileData(context: Context) {
        loadCachedHomeSummary(context)
        viewModelScope.launch {
            fetchUserDataNow(context.applicationContext)
        }
    }

    fun clearBackendUser() {
        backendUser = null
        userName = "Loading..."
        userPoints = 0
    }

    fun fetchUserData(context: Context? = null) {
        viewModelScope.launch {
            fetchUserDataNow(context?.applicationContext)
        }
    }

    private suspend fun refreshBackendUserSummaryNow(context: Context) {
        val appContext = context.applicationContext
        val authRepository = AuthRepository(appContext)
        val pointsRepository = PointsRepository(appContext)

        when (val result = authRepository.me()) {
            is AuthResult.Success -> {
                applyBackendUser(result.value)
                cacheBackendUser(appContext, result.value)
            }
            is AuthResult.Error -> Unit
        }

        when (val result = pointsRepository.getMyPoints()) {
            is AuthResult.Success -> {
                applyBackendPoints(result.value.total, result.value.lifetimeTotal)
                cacheBackendPoints(appContext, result.value.total, result.value.lifetimeTotal)
            }
            is AuthResult.Error -> Unit
        }
    }

    private suspend fun fetchBackendUserDataNow(context: Context): Boolean {
        val appContext = context.applicationContext
        val authRepository = AuthRepository(appContext)
        if (!authRepository.isLoggedIn()) {
            return false
        }

        val pointsRepository = PointsRepository(appContext)
        val rewardRepository = RewardRepository(appContext)
        val recyclingRepository = RecyclingRepository(appContext)

        when (val result = authRepository.me()) {
            is AuthResult.Success -> {
                applyBackendUser(result.value)
                cacheBackendUser(appContext, result.value)
            }
            is AuthResult.Error -> {
                loadCachedHomeSummary(appContext)
                return true
            }
        }

        when (val result = pointsRepository.getMyPoints()) {
            is AuthResult.Success -> {
                applyBackendPoints(result.value.total, result.value.lifetimeTotal)
                cacheBackendPoints(appContext, result.value.total, result.value.lifetimeTotal)
            }
            is AuthResult.Error -> Unit
        }

        when (val result = rewardRepository.getRewards()) {
            is AuthResult.Success -> {
                rewardsCatalog.clear()
                rewardsCatalog.addAll(result.value)
            }
            is AuthResult.Error -> Unit
        }

        when (val result = rewardRepository.getMyRedemptions()) {
            is AuthResult.Success -> {
                redemptionHistory.clear()
                redemptionHistory.addAll(result.value)
                totalRedemptions = result.value.size
            }
            is AuthResult.Error -> Unit
        }

        when (val result = recyclingRepository.getMySubmissions()) {
            is AuthResult.Success -> {
                recyclingHistory.clear()
                recyclingHistory.addAll(result.value)

                val approvedLogs = result.value.filter { it.status == "Approved" }
                plasticKg = approvedLogs.filter { it.material_type == "Plastic" }.sumOf { it.quantity }.toFloat()
                paperKg = approvedLogs.filter { it.material_type == "Paper" }.sumOf { it.quantity }.toFloat()
                glassKg = approvedLogs.filter { it.material_type == "Glass" }.sumOf { it.quantity }.toFloat()
                metalKg = approvedLogs.filter { it.material_type == "Metal" }.sumOf { it.quantity }.toFloat()

                val sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 3600).toString()
                val recentLogs = approvedLogs.filter {
                    it.created_at != null && it.created_at >= sevenDaysAgo
                }
                streakDays = recentLogs.mapNotNull { it.created_at?.take(10) }.toSet().size
            }
            is AuthResult.Error -> Unit
        }

        return true
    }

    private suspend fun fetchUserDataNow(context: Context? = null) {
        isRefreshing = true
        try {
            if (context != null) {
                loadCachedHomeSummary(context)
                if (!context.isNetworkAvailable()) {
                    return
                }
            }

            if (context != null && fetchBackendUserDataNow(context)) {
                return
            }

            val user = supabase.auth.currentUserOrNull()
            if (user == null) {
                if (backendUser == null) {
                    userName = "Not Logged In"
                    userPoints = 0
                } else {
                    userName = backendUser?.name ?: "User"
                }
                return
            }

            try {
                val profile = supabase.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingle<Profile>()
                if (backendUser == null) {
                    userName = profile.full_name ?: profile.username ?: user.email?.substringBefore("@") ?: "User"
                    userPoints = profile.total_points
                }
            } catch (e: Exception) {
                if (backendUser == null) {
                    userName = user.email?.substringBefore("@") ?: "User"
                }
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

            try {
                val profile = supabase.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingle<Profile>()
                if (backendUser == null) {
                    lifetimePoints = profile.lifetime_points
                }

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
            if (backendUser == null) {
                context?.let { loadCachedHomeSummary(it) }
                if (userName == "Loading...") {
                    userName = "User"
                }
            }
        } finally {
            isRefreshing = false
        }
    }

    private fun cacheBackendUser(context: Context, user: AuthUser) {
        context.applicationContext.getSharedPreferences(HOME_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOME_USER_NAME, user.name)
            .apply()
    }

    private fun cacheBackendPoints(context: Context, points: Int, lifetime: Int) {
        context.applicationContext.getSharedPreferences(HOME_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOME_POINTS, points)
            .putInt(KEY_HOME_LIFETIME_POINTS, lifetime)
            .apply()
    }

    /**
     * Fetch leaderboard for a specific timeframe: "daily", "weekly", or "all_time".
     * Routes to the correct algorithm in LeaderboardEngine including anti-gaming gate.
     */
    fun fetchLeaderboard(timeframe: String = "all_time", context: Context? = null) {
        viewModelScope.launch {
            isLoadingLeaderboard = true
            currentLeaderboardTimeframe = timeframe
            try {
                val response = if (context != null && AuthRepository(context.applicationContext).isLoggedIn()) {
                    when (val result = LeaderboardRepository(context.applicationContext).getLeaderboard(timeframe)) {
                        is AuthResult.Success -> result.value
                        is AuthResult.Error -> LeaderboardResponse(timeframe, emptyList(), Instant.now().toString())
                    }
                } else {
                    leaderboardEngine.getLeaderboard(timeframe, includeRankChange = true)
                }
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

    fun showPopOut(
        title: String,
        message: String,
        type: PopOutMessageType = PopOutMessageType.Info,
        buttonText: String = "Got it"
    ) {
        popOutMessage = AppPopOutMessage(
            title = title,
            message = friendlyUserMessage(message),
            type = type,
            buttonText = buttonText
        )
    }

    fun dismissPopOut() {
        popOutMessage = null
    }

    fun redeemItem(reward: Reward, quantity: Int = 1, context: android.content.Context) {
        viewModelScope.launch {
            val rewardId = reward.id
            if (rewardId == null) {
                showPopOut(
                    title = "Reward Unavailable",
                    message = "This reward cannot be redeemed right now. Please try another reward.",
                    type = PopOutMessageType.Error
                )
                return@launch
            }

            when (val result = RewardRepository(context.applicationContext).redeemReward(rewardId, quantity)) {
                is AuthResult.Success -> {
                    fetchUserData(context)
                    showPopOut(
                        title = "Reward Reserved",
                        message = "$quantity x \"${reward.name}\" has been reserved for ${reward.points_required * quantity} points.",
                        type = PopOutMessageType.Success
                    )
                }
                is AuthResult.Error -> {
                    showPopOut(
                        title = "Redemption Failed",
                        message = result.message,
                        type = PopOutMessageType.Error
                    )
                }
            }
        }
    }
    suspend fun submitRecyclingLog(
        materialType: String,
        quantity: Double,
        context: android.content.Context,
        proofPhotoUri: Uri? = null,
        proofPhotoBitmap: Bitmap? = null
    ): Boolean {
        val appContext = context.applicationContext
        val repository = RecyclingRepository(appContext)
        val proofPayload = buildRecyclingProofPayload(appContext, proofPhotoUri, proofPhotoBitmap)
        if (proofPayload == null) {
            showPopOut(
                title = "Photo Required",
                message = "Please choose or take a clear proof photo before submitting your deposit.",
                type = PopOutMessageType.Error
            )
            return false
        }

        val upload = when (val result = repository.uploadRecyclingProof(
            proofPayload.bytes,
            proofPayload.mimeType,
            proofPayload.fileName
        )) {
            is AuthResult.Success -> result.value
            is AuthResult.Error -> {
                showPopOut(
                    title = "Photo Upload Failed",
                    message = result.message,
                    type = PopOutMessageType.Error
                )
                return false
            }
        }

        return when (val result = repository.createSubmission(
            materialType = materialType,
            quantity = quantity,
            proofImageUrl = upload.fileUrl,
            uploadId = upload.id
        )) {
            is AuthResult.Success -> {
                fetchUserData(context)
                showPopOut(
                    title = "Deposit Submitted",
                    message = "Your ${quantity}kg of $materialType has been submitted for review. Points will be awarded after approval.",
                    type = PopOutMessageType.Success
                )
                true
            }
            is AuthResult.Error -> {
                showPopOut(
                    title = "Submission Failed",
                    message = result.message,
                    type = PopOutMessageType.Error
                )
                false
            }
        }
    }

    suspend fun claimRecyclingQrPayload(
        rawClaimPayload: String,
        context: android.content.Context
    ): AuthResult<RecyclingLog> {
        val repository = RecyclingRepository(context.applicationContext)
        val result = repository.claimQr(rawClaimPayload)
        if (result is AuthResult.Success) {
            fetchUserData(context)
        }
        return result
    }

    private fun friendlyUserMessage(message: String): String {
        return message
            .replace("backend", "EcoRecycle services", ignoreCase = true)
            .replace("Docker", "the service", ignoreCase = true)
            .replace("port 5000", "your connection", ignoreCase = true)
    }

    private fun buildRecyclingProofPayload(
        context: Context,
        proofPhotoUri: Uri?,
        proofPhotoBitmap: Bitmap?
    ): RecyclingProofPayload? {
        if (proofPhotoUri != null) {
            val mimeType = context.contentResolver.getType(proofPhotoUri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(proofPhotoUri)?.use { it.readBytes() }
                ?: return null
            return RecyclingProofPayload(
                bytes = bytes,
                mimeType = mimeType,
                fileName = "recycling-proof-${System.currentTimeMillis()}.${extensionForMimeType(mimeType)}"
            )
        }

        if (proofPhotoBitmap != null) {
            val bytes = ByteArrayOutputStream().use { output ->
                proofPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                output.toByteArray()
            }
            return RecyclingProofPayload(
                bytes = bytes,
                mimeType = "image/jpeg",
                fileName = "recycling-proof-${System.currentTimeMillis()}.jpg"
            )
        }

        return null
    }

    private fun extensionForMimeType(mimeType: String): String =
        when (mimeType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
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
