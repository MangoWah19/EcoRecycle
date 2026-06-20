package com.example.fyp1

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ============================================
// PROFILE & USER DATA
// ============================================

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    val full_name: String? = null,
    val total_points: Int = 0,
    val lifetime_points: Int = 0,
    val role: String? = "user",
    val created_at: String? = null,
    val last_redemption_at: String? = null,
    val redemption_count_today: Int = 0,
    val last_log_submission_at: String? = null,
    val suspicious_activity_flagged: Boolean = false
)

// ============================================
// LEADERBOARD DATA
// ============================================

@Serializable
data class LeaderboardEntry(
    val full_name: String?,
    val lifetime_points: Int,
    val total_points: Int,
    val role: String?,
    val id: String? = null
)

@Serializable
data class LeaderboardSnapshot(
    val id: Long? = null,
    val snapshot_date: String,
    val timeframe: String,
    val user_id: String,
    val rank: Int,
    val points: Int,
    val created_at: String? = null
)

@Serializable
data class RankChangeIndicator(
    val user_id: String,
    val user_name: String,
    val current_rank: Int,
    val previous_rank: Int,
    val rank_change: Int,
    val rank_change_indicator: String = when {
        rank_change > 0 -> "↑"
        rank_change < 0 -> "↓"
        else -> "→"
    }
)

// ============================================
// RECYCLING LOGS
// ============================================

@Serializable
data class RecyclingLog(
    val id: Int? = null,
    val user_id: String,
    val material_type: String,
    val quantity: Double,
    val status: String,
    val points_awarded: Int,
    val created_at: String? = null,
    val is_duplicate_flagged: Boolean = false,
    val admin_notes: String? = null
)

// ============================================
// REDEMPTIONS
// ============================================

@Serializable
data class Redemption(
    val id: Long? = null,
    val user_id: String,
    val reward_id: Int? = null,
    val item_name: String,
    val points_spent: Int,
    val created_at: String? = null,
    val status: String = "completed",
    val claimed_at: String? = null,
    val expires_at: String? = null
)

// ============================================
// REWARDS & REWARDS CATALOG
// ============================================

@Serializable
data class Reward(
    val id: Int? = null,
    val name: String,
    @SerialName("points_required")
    val points_required: Int,
    val stock: Int = 0,
    @SerialName("image_url")
    val image_url: String? = null,
    val category: String? = null,
    val expires_at: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Serializable
data class RedemptionCooldown(
    val id: Long? = null,
    val user_id: String,
    val reward_id: Int,
    val last_redeemed_at: String? = null,
    val count_today: Int = 0,
    val count_week: Int = 0
)

// ============================================
// ACHIEVEMENTS & BADGES
// ============================================

@Serializable
data class Achievement(
    val id: Long? = null,
    val user_id: String,
    val achievement_type: String,
    val unlocked_at: String? = null
)

// UI model — includes progress tracking for the achievements screen
data class AchievementBadge(
    val type: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val current: Double = 0.0,
    val target: Double = 1.0,
    val unit: String = ""
)

// ============================================
// ANTI-GAMING & VALIDATION
// ============================================

@Serializable
data class SuspiciousActivity(
    val user_id: String,
    val activity_type: String,
    val severity: String,
    val detected_at: String,
    val details: String? = null
)

data class ValidationResult(val isValid: Boolean, val issues: List<String>)
data class RedemptionEligibility(val canRedeem: Boolean, val issues: List<String>)
data class CooldownInfo(val canRedeem: Boolean, val cooldownReason: String)
data class PointBalance(val totalPoints: Int, val lifetimePoints: Int)
data class RedemptionResult(val success: Boolean, val message: String, val redemptionId: Long?)
data class EligibilityResult(val isEligible: Boolean, val reasons: List<String>)
data class CooldownCheckResult(val canRedeem: Boolean, val hoursRemaining: Int)
data class RedemptionWithState(val redemption: Redemption, val currentState: String)

data class UserStatistics(
    val totalSubmissions: Int = 0,
    val plasticRecycled: Double = 0.0,
    val paperRecycled: Double = 0.0,
    val glassRecycled: Double = 0.0,
    val metalRecycled: Double = 0.0,
    val totalRedemptions: Int = 0,
    val currentStreak: Int = 0,
    val isPlasticKing: Boolean = false,
    val isPaperMaster: Boolean = false,
    val isGlassGuard: Boolean = false,
    val isMetalMaven: Boolean = false,
    val isWeekStreak: Boolean = false,
    val isSpeedRecycler: Boolean = false,
    val isEcoWarrior: Boolean = false
)

// UI model for the recycling guide screen
data class MaterialGuide(
    val name: String,
    val points: String,
    val color: Color,
    val icon: ImageVector
)

// ============================================
// LEADERBOARD RESPONSE MODELS
// ============================================

@Serializable
data class LeaderboardResponse(
    val timeframe: String,
    val entries: List<LeaderboardEntryWithRank>,
    val generated_at: String
)

@Serializable
data class LeaderboardEntryWithRank(
    val rank: Int,
    val full_name: String?,
    val lifetime_points: Int,
    val total_points: Int,
    val user_id: String? = null,
    val rank_change: String? = null
)

// ============================================
// CATEGORY-BASED RANKINGS
// ============================================

@Serializable
data class CategoryLeaderboard(
    val category: String,
    val entries: List<CategoryLeaderboardEntry>
)

@Serializable
data class CategoryLeaderboardEntry(
    val rank: Int,
    val user_name: String,
    val material_type: String,
    val total_quantity: Double,
    val total_points: Int
)