package com.example.fyp1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Redemption(
    val id: String? = null,
    val user_id: String,
    val reward_id: String? = null,
    val item_name: String,
    val quantity: Int = 1,
    val points_spent: Int,
    val created_at: String? = null,
    val status: String = "RESERVED",
    val reserved_at: String? = null,
    val claimed_at: String? = null,
    val completed_at: String? = null,
    val cancelled_at: String? = null,
    val cancel_reason: String? = null,
    val expires_at: String? = null
)

@Serializable
data class Reward(
    val id: String? = null,
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
    val id: String? = null,
    val user_id: String,
    val reward_id: String,
    val last_redeemed_at: String? = null,
    val count_today: Int = 0,
    val count_week: Int = 0
)

data class RedemptionEligibility(val canRedeem: Boolean, val issues: List<String>)
data class CooldownInfo(val canRedeem: Boolean, val cooldownReason: String)
data class RedemptionResult(val success: Boolean, val message: String, val redemptionId: String?)
data class EligibilityResult(val isEligible: Boolean, val reasons: List<String>)
data class CooldownCheckResult(val canRedeem: Boolean, val hoursRemaining: Int)
data class RedemptionWithState(val redemption: Redemption, val currentState: String)
