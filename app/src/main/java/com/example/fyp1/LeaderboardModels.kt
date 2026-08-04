package com.example.fyp1

import kotlinx.serialization.Serializable

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
        rank_change > 0 -> "\u2191"
        rank_change < 0 -> "\u2193"
        else -> "\u2192"
    }
)

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
