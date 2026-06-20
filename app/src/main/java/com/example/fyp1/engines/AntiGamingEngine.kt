package com.example.fyp1.engines

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
import com.example.fyp1.utils.getHoursSince

class AntiGamingEngine(private val supabaseClient: Any) {

    private val supabase get() = supabaseClient as io.github.jan.supabase.SupabaseClient

    suspend fun validateRecyclingSubmission(
        userId: String,
        materialType: String,
        quantity: Double
    ): ValidationResult {
        val issues = mutableListOf<String>()

        try {
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<Profile>()

            if (profile.suspicious_activity_flagged) {
                return ValidationResult(
                    isValid = false,
                    issues = listOf("Your account has been flagged for suspicious activity. Please contact your administrator.")
                )
            }
        } catch (e: Exception) {
            // Silent fail 闂?do not block if profile check fails
        }

        // Point rate per material (must match UI rates)
        val ratePerKg = POINT_RATES[materialType] ?: 20
        val estimatedPoints = (quantity * ratePerKg).toInt()

        // 1. Submission cooldown check
        val timeSinceLastSubmission = getTimeSinceLastSubmission(userId)
        if (timeSinceLastSubmission < MIN_SUBMISSION_INTERVAL) {
            val secondsRemaining = MIN_SUBMISSION_INTERVAL - timeSinceLastSubmission
            issues.add("Too soon! Please wait ${secondsRemaining}s before submitting again. (Minimum: 1 submission per minute)")
        }

        // 2. Duplicate submission check
        if (isDuplicateSubmission(userId, materialType, quantity)) {
            issues.add("Duplicate detected: You already submitted ${quantity}kg of $materialType within the last 5 minutes. Please wait before resubmitting the same item.")
            flagSuspiciousActivity(
                userId = userId,
                activityType = "duplicate_submission",
                severity = "medium",
                details = "Duplicate: ${quantity}kg of $materialType submitted within 5-minute window."
            )
        }

// 3. Hourly submission count check
        val submissionsThisHour = countSubmissionsInTimeWindow(userId, 3600)
        if (submissionsThisHour >= MAX_SUBMISSIONS_PER_HOUR) {
            issues.add("Hourly limit reached: You have made $submissionsThisHour/$MAX_SUBMISSIONS_PER_HOUR submissions this hour. Please try again after the hour resets.")
            flagSuspiciousActivity(
                userId = userId,
                activityType = "hourly_limit_exceeded",
                severity = "high",
                details = "Made $submissionsThisHour submissions in one hour (limit: $MAX_SUBMISSIONS_PER_HOUR)."
            )
        }

        // 4. Daily points check 闂?includes what THIS submission would earn
        val dailyPoints = calculateDailyPointsEarned(userId)
        val projectedTotal = dailyPoints + estimatedPoints

        when {
            // Already at or over the limit
            dailyPoints >= MAX_DAILY_POINTS -> {
                issues.add("Daily points limit reached: You have already earned $dailyPoints/$MAX_DAILY_POINTS pts today. Come back tomorrow to continue earning!")
            }
            // This single submission alone exceeds the daily limit
            estimatedPoints > MAX_DAILY_POINTS -> {
                val maxAllowedKg = MAX_DAILY_POINTS.toDouble() / ratePerKg
                issues.add("Submission Too Large: ${quantity}kg of $materialType would award ~$estimatedPoints pts, which exceeds the daily limit of $MAX_DAILY_POINTS pts. Maximum allowed for $materialType is ${String.format("%.1f", maxAllowedKg)}kg per day.")
                flagSuspiciousActivity(
                    userId = userId,
                    activityType = "oversized_submission",
                    severity = "high",
                    details = "Attempted ${quantity}kg of $materialType (~$estimatedPoints pts), exceeds $MAX_DAILY_POINTS pt daily cap."
                )
            }
            // This submission would push them over the limit
            projectedTotal > MAX_DAILY_POINTS -> {
                val remainingPoints = MAX_DAILY_POINTS - dailyPoints
                val maxAllowedKg = remainingPoints.toDouble() / ratePerKg
                issues.add("Would Exceed Daily Limit: You have $dailyPoints pts today. This submission would add ~$estimatedPoints pts (total: $projectedTotal pts), exceeding the $MAX_DAILY_POINTS pt daily limit. You can submit up to ${String.format("%.1f", maxAllowedKg)}kg of $materialType today.")
            }
        }

        return ValidationResult(issues.isEmpty(), issues)
    }

    private suspend fun getTimeSinceLastSubmission(userId: String): Long {
        return try {
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<Profile>()

            profile.last_log_submission_at?.let { lastSubmitTime ->
                val lastSubmit = Instant.parse(lastSubmitTime)
                val now = Instant.now()
                java.time.temporal.ChronoUnit.SECONDS.between(lastSubmit, now)
            } ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    private suspend fun countSubmissionsInTimeWindow(
        userId: String,
        windowSeconds: Long
    ): Int {
        return try {
            val cutoffTime = Instant.now()
                .minusSeconds(windowSeconds)
                .toString()

            val logs = supabase.postgrest["recycling_logs"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("created_at", cutoffTime)
                    }
                }
                .decodeList<RecyclingLog>()

            logs.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun isDuplicateSubmission(
        userId: String,
        materialType: String,
        quantity: Double
    ): Boolean {
        return try {
            val cutoffTime = Instant.now()
                .minusSeconds(DUPLICATE_DETECTION_WINDOW.toLong())
                .toString()

            val recentLogs = supabase.postgrest["recycling_logs"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("material_type", materialType)
                        gte("created_at", cutoffTime)
                    }
                }
                .decodeList<RecyclingLog>()

            recentLogs.any { log ->
                val diff = kotlin.math.abs(log.quantity - quantity)
                val margin = quantity * 0.1
                diff <= margin
            }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun calculateDailyPointsEarned(userId: String): Int {
        return try {
            val todayStart = LocalDateTime.now()
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toString()

            val todayLogs = supabase.postgrest["recycling_logs"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", "Approved")
                        gte("created_at", todayStart)
                    }
                }
                .decodeList<RecyclingLog>()

            todayLogs.sumOf { it.points_awarded }
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun countRedemptionsInTimeWindow(
        userId: String,
        windowSeconds: Long
    ): Int {
        return try {
            val cutoffTime = Instant.now()
                .minusSeconds(windowSeconds)
                .toString()

            val redemptions = supabase.postgrest["redemptions"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("created_at", cutoffTime)
                    }
                }
                .decodeList<Redemption>()

            redemptions.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getRewardCooldownInfo(
        userId: String,
        rewardId: Int
    ): CooldownInfo {
        return try {
            val cooldown = supabase.postgrest["redemption_cooldowns"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("reward_id", rewardId)
                    }
                }
                .decodeSingleOrNull<RedemptionCooldown>()

            if (cooldown == null) {
                return CooldownInfo(canRedeem = true, cooldownReason = "")
            }

            val lastRedeemed = cooldown.last_redeemed_at?.let { Instant.parse(it) }
            if (lastRedeemed == null) {
                return CooldownInfo(canRedeem = true, cooldownReason = "")
            }

            val hoursSince = getHoursSince(lastRedeemed)
            val requiredCooldown = if (isRareReward(rewardId)) 72 else 24

            if (hoursSince >= requiredCooldown) {
                CooldownInfo(canRedeem = true, cooldownReason = "")
            } else {
                val hoursRemaining = (requiredCooldown - hoursSince).toInt()
                CooldownInfo(
                    canRedeem = false,
                    cooldownReason = "Cooldown active: This reward was recently redeemed. You must wait $hoursRemaining more hour(s) before redeeming it again."
                )
            }
        } catch (e: Exception) {
            CooldownInfo(canRedeem = true, cooldownReason = "")
        }
    }

    private suspend fun getRewardInfo(rewardId: Int): Reward? {
        return try {
            supabase.postgrest["rewards_catalog"]
                .select { filter { eq("id", rewardId) } }
                .decodeSingleOrNull<Reward>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getUserProfile(userId: String): Profile? {
        return try {
            supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun flagSuspiciousActivity(
        userId: String,
        activityType: String,
        severity: String,
        details: String?
    ) {
        try {
            supabase.postgrest["suspicious_activity_logs"].insert(
                SuspiciousActivity(
                    user_id = userId,
                    activity_type = activityType,
                    severity = severity,
                    detected_at = Instant.now().toString(),
                    details = details
                )
            )

            supabase.postgrest["profiles"].update(
                mapOf("suspicious_activity_flagged" to true)
            ) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            // Silent fail - do not block user flow
        }
    }

    // Called from redeemItem 闂?checks if cooldown is active before allowing redemption
    suspend fun checkRedemptionCooldown(userId: String, rewardId: Int): CooldownInfo {
        return getRewardCooldownInfo(userId, rewardId)
    }

    // Called from redeemItem 闂?saves the timestamp so cooldown starts counting
    suspend fun recordCooldownAfterRedemption(userId: String, rewardId: Int) {
        try {
            val existing = supabase.postgrest["redemption_cooldowns"]
                .select { filter { eq("user_id", userId); eq("reward_id", rewardId) } }
                .decodeSingleOrNull<RedemptionCooldown>()

            if (existing != null) {
                supabase.postgrest["redemption_cooldowns"].update(
                    mapOf(
                        "last_redeemed_at" to Instant.now().toString(),
                        "count_today" to (existing.count_today + 1),
                        "count_week" to (existing.count_week + 1)
                    )
                ) { filter { eq("id", existing.id!!) } }
            } else {
                supabase.postgrest["redemption_cooldowns"].insert(
                    RedemptionCooldown(
                        user_id = userId,
                        reward_id = rewardId,
                        last_redeemed_at = Instant.now().toString(),
                        count_today = 1,
                        count_week = 1
                    )
                )
            }
        } catch (e: Exception) { /* Silent fail */ }
    }

    // Called after each recycling submission 闂?checks material totals and unlocks badges
    suspend fun checkAndUnlockRecyclingAchievements(userId: String) {
        try {
            val logs = supabase.postgrest["recycling_logs"]
                .select { filter { eq("user_id", userId); eq("status", "Approved") } }
                .decodeList<RecyclingLog>()

            val plasticKg  = logs.filter { it.material_type == "Plastic" }.sumOf { it.quantity }
            val paperKg    = logs.filter { it.material_type == "Paper"   }.sumOf { it.quantity }
            val glassKg    = logs.filter { it.material_type == "Glass"   }.sumOf { it.quantity }
            val metalKg    = logs.filter { it.material_type == "Metal"   }.sumOf { it.quantity }

            // Fetch lifetime points for eco_warrior check
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
            val lifetimePoints = profile?.lifetime_points ?: 0

            // Check streak 闂?count distinct days with approved logs in last 7 days
            val sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 3600).toString()
            val recentLogs = logs.filter {
                it.created_at != null && it.created_at >= sevenDaysAgo
            }
            val distinctDays = recentLogs.mapNotNull { it.created_at?.take(10) }.toSet().size

            if (plasticKg  >= 100) unlockAchievement(userId, "plastic_king")
            if (paperKg    >= 50)  unlockAchievement(userId, "paper_master")
            if (glassKg    >= 75)  unlockAchievement(userId, "glass_guard")
            if (lifetimePoints >= 1000) unlockAchievement(userId, "eco_warrior")
            if (distinctDays   >= 7)   unlockAchievement(userId, "week_streak")
        } catch (e: Exception) { /* Silent fail */ }
    }

    private suspend fun unlockAchievement(userId: String, achievementType: String) {
        try {
            val existing = supabase.postgrest["achievement_unlocks"]
                .select { filter { eq("user_id", userId); eq("achievement_type", achievementType) } }
                .decodeSingleOrNull<Achievement>()

            if (existing == null) {
                supabase.postgrest["achievement_unlocks"].insert(
                    Achievement(
                        user_id = userId,
                        achievement_type = achievementType,
                        unlocked_at = Instant.now().toString()
                    )
                )
            }
        } catch (e: Exception) { /* Silent fail */ }
    }

    private suspend fun isRareReward(rewardId: Int): Boolean {
        return try {
            val reward = getRewardInfo(rewardId)
            reward?.points_required?.let { it > 500 } ?: false
        } catch (e: Exception) {
            false
        }
    }
}


// ============================================
// POINTS LEDGER  (single source of truth for all point mutations)
// ============================================

/**
 * PointsLedger is the ONLY place that reads or writes point balances.
 * RewardsEngine calls getUserPointBalance() for eligibility checks (read-only).
 * redeemItem() calls spendPoints() for deductions.
 * Admin approval flow calls awardPoints() when a recycling log is approved.
 *
 * This separation follows the Command-Query Responsibility Segregation (CQRS)
 * pattern recommended for gamified point economies:
 *   Paharia, R. (2013). Loyalty 3.0. McGraw-Hill. Chapter 5 闂?Points Economy.
 *
 * NOTE: lifetime_points is NEVER reduced on spend 闂?it is used exclusively
 * for all-time leaderboard ranking. Only total_points (spendable balance) is
 * decremented on reward redemption. This matches the earn-vs-spend ledger model
 * used by apps like Habitica and Duolingo (Hamari & Koivisto, 2015).
 */
