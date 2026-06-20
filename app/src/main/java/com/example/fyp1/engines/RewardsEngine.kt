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

class RewardsEngine(private val supabaseClient: Any) {

    private val supabase get() = supabaseClient as io.github.jan.supabase.SupabaseClient

    /**
     * REWARD STATE LIFECYCLE:
     *   available  闂?Reward exists in rewards_catalog with is_active=true and stock > 0
     *   claimed    闂?User redeemed; redemption row inserted with status="claimed"
     *   redeemed   闂?Physical reward collected; status transitioned to "redeemed"
     *   expired    闂?Claimed but not collected within DEFAULT_CLAIM_EXPIRY_DAYS
     *
     * This lifecycle mirrors the model described in:
     *   Zichermann, G. & Cunningham, C. (2011). Gamification by Design.
     *   O'Reilly Media. Chapter 4 闂?Reward Structures.
     */

    /**
     * Full eligibility check using PointsLedger (read-only totals).
     * No point mutations happen here 闂?pure validation only.
     */
    suspend fun checkEligibility(userId: String, reward: Reward): EligibilityResult {
        val issues = mutableListOf<String>()

        // 1. Stock check
        if (reward.stock <= 0) issues.add("Out of stock")

        // 2. Points check via ledger (read-only 闂?no mutation)
        val ledger = PointsLedger(supabase)
        val balance = ledger.getUserPointBalance(userId)
        if (balance.totalPoints < reward.points_required) {
            val needed = reward.points_required - balance.totalPoints
            issues.add("Need $needed more points (have ${balance.totalPoints}, need ${reward.points_required})")
        }

        // 3. Active status check
        if (!reward.is_active) issues.add("Reward is no longer active")

        // 4. Catalog-level expiry check
        reward.expires_at?.let { exp ->
            if (Instant.now().isAfter(Instant.parse(exp))) issues.add("Reward offer has expired")
        }

        return EligibilityResult(isEligible = issues.isEmpty(), reasons = issues)
    }

    /**
     * Transition: claimed 闂?redeemed
     * Called when the physical reward is confirmed as collected (admin or QR scan).
     * Only a redemption currently in "claimed" state can be moved to "redeemed".
     */
    suspend fun markAsRedeemed(redemptionId: Long): Boolean {
        return try {
            val redemption = supabase.postgrest["redemptions"]
                .select { filter { eq("id", redemptionId) } }
                .decodeSingleOrNull<Redemption>()

            // Guard: only "claimed" redemptions can be marked as redeemed
            if (redemption?.status != "claimed") return false

            supabase.postgrest["redemptions"].update(
                mapOf(
                    "status" to "redeemed",
                    "claimed_at" to Instant.now().toString()
                )
            ) {
                filter { eq("id", redemptionId) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Transition: claimed 闂?expired
     * Bulk-expires all "claimed" redemptions whose expires_at has passed.
     * Called before each new redemption to keep the user's history clean.
     */
    suspend fun expireStaleRedemptions(userId: String) {
        try {
            val now = Instant.now().toString()
            supabase.postgrest["redemptions"].update(
                mapOf("status" to "expired")
            ) {
                filter {
                    eq("user_id", userId)
                    eq("status", "claimed")
                    lt("expires_at", now)
                }
            }
        } catch (e: Exception) {
            // Silent 闂?do not block main redemption flow
        }
    }

    private suspend fun createRedemption(userId: String, reward: Reward): Redemption? {
        return try {
            val expiryDate = Instant.now()
                .plusSeconds((DEFAULT_CLAIM_EXPIRY_DAYS * 24 * 3600).toLong())
                .toString()

            val redemption = Redemption(
                user_id = userId,
                reward_id = reward.id,
                item_name = reward.name,
                points_spent = reward.points_required,
                status = "claimed",
                claimed_at = Instant.now().toString(),
                expires_at = expiryDate
            )

            supabase.postgrest["redemptions"]
                .insert(redemption)
                .decodeSingle<Redemption>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun addPointsBack(userId: String, amount: Int): Boolean {
        return try {
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<Profile>()

            val newBalance = profile.total_points + amount

            supabase.postgrest["profiles"].update(
                mapOf("total_points" to newBalance)
            ) {
                filter { eq("id", userId) }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun decreaseRewardStock(rewardId: Int): Boolean {
        return try {
            val reward = supabase.postgrest["rewards_catalog"]
                .select { filter { eq("id", rewardId) } }
                .decodeSingle<Reward>()

            if (reward.stock <= 0) return false

            supabase.postgrest["rewards_catalog"].update(
                mapOf("stock" to (reward.stock - 1))
            ) {
                filter { eq("id", rewardId) }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun markRedemptionCompleted(redemptionId: Long): Boolean {
        return try {
            supabase.postgrest["redemptions"].update(
                mapOf("status" to "completed")
            ) {
                filter { eq("id", redemptionId) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun recordRedemptionCooldown(userId: String, rewardId: Int) {
        try {
            val existing = supabase.postgrest["redemption_cooldowns"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("reward_id", rewardId)
                    }
                }
                .decodeSingleOrNull<RedemptionCooldown>()

            if (existing != null) {
                supabase.postgrest["redemption_cooldowns"].update(
                    mapOf(
                        "last_redeemed_at" to Instant.now().toString(),
                        "count_today" to (existing.count_today + 1),
                        "count_week" to (existing.count_week + 1)
                    )
                ) {
                    filter { eq("id", existing.id!!) }
                }
            } else {
                val cooldown = RedemptionCooldown(
                    user_id = userId,
                    reward_id = rewardId,
                    last_redeemed_at = Instant.now().toString(),
                    count_today = 1,
                    count_week = 1
                )
                supabase.postgrest["redemption_cooldowns"].insert(cooldown)
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    private suspend fun countUserRedemptionsOfReward(
        userId: String,
        rewardId: Int,
        hoursBack: Int
    ): Int {
        return try {
            val cutoffTime = Instant.now()
                .minusSeconds((hoursBack * 3600).toLong())
                .toString()

            val redemptions = supabase.postgrest["redemptions"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("reward_id", rewardId)
                        gte("created_at", cutoffTime)
                    }
                }
                .decodeList<Redemption>()

            redemptions.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getActiveRewards(): List<Reward> {
        return try {
            supabase.postgrest["rewards_catalog"]
                .select {
                    filter {
                        eq("is_active", true)
                        gt("stock", 0)
                    }
                }
                .decodeList<Reward>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getRewardFromDb(rewardId: Int): Reward? {
        return try {
            supabase.postgrest["rewards_catalog"]
                .select { filter { eq("id", rewardId) } }
                .decodeSingleOrNull<Reward>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getRedemptionFromDb(redemptionId: Long): Redemption? {
        return try {
            supabase.postgrest["redemptions"]
                .select { filter { eq("id", redemptionId) } }
                .decodeSingleOrNull<Redemption>()
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

    private suspend fun getTotalRedemptions(userId: String): Int {
        return try {
            val redemptions = supabase.postgrest["redemptions"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<Redemption>()
            redemptions.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun unlockAchievement(userId: String, achievementType: String) {
        try {
            val existing = supabase.postgrest["achievement_unlocks"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("achievement_type", achievementType)
                    }
                }
                .decodeSingleOrNull<Achievement>()

            if (existing == null) {
                val achievement = Achievement(
                    user_id = userId,
                    achievement_type = achievementType,
                    unlocked_at = Instant.now().toString()
                )
                supabase.postgrest["achievement_unlocks"].insert(achievement)
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    private suspend fun checkRedemptionAchievements(userId: String) {
        val redemptionCount = getTotalRedemptions(userId)

        when {
            redemptionCount == 1 -> unlockAchievement(userId, "first_redemption")
            redemptionCount == 10 -> unlockAchievement(userId, "reward_collector")
            redemptionCount == 50 -> unlockAchievement(userId, "reward_master")
        }
    }

    // Public version called from redeemItem in MainViewModel
    suspend fun checkAndUnlockRedemptionAchievements(userId: String) {
        checkRedemptionAchievements(userId)
    }

    private suspend fun getRedemptionCooldown(userId: String, rewardId: Int): RedemptionCooldown? {
        return try {
            supabase.postgrest["redemption_cooldowns"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("reward_id", rewardId)
                    }
                }
                .decodeSingleOrNull<RedemptionCooldown>()
        } catch (e: Exception) {
            null
        }
    }
}


