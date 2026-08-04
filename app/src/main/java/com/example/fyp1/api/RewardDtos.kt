package com.example.fyp1.api

data class RewardsData(
    val rewards: List<BackendReward> = emptyList()
)

data class RewardData(
    val reward: BackendReward
)

data class RedeemRewardRequest(
    val quantity: Int = 1
)

data class RedemptionsData(
    val redemptions: List<BackendRedemption> = emptyList()
)

data class RedemptionData(
    val redemption: BackendRedemption
)

data class BackendReward(
    val id: String,
    val name: String,
    val pointsRequired: Int,
    val stock: Int = 0,
    val imageUrl: String? = null,
    val category: String? = null,
    val expiresAt: String? = null,
    val isActive: Boolean = true,
    val tier: String? = null,
    val createdAt: String? = null
)

data class BackendRedemption(
    val id: String,
    val userId: String,
    val rewardId: String? = null,
    val itemName: String,
    val quantity: Int = 1,
    val pointsSpent: Int,
    val status: String = "RESERVED",
    val reservedAt: String? = null,
    val claimedAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null
)
