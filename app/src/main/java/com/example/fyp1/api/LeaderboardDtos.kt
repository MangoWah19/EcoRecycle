package com.example.fyp1.api

data class BackendLeaderboardData(
    val timeframe: String,
    val generated_at: String,
    val entries: List<BackendLeaderboardEntry> = emptyList()
)

data class BackendLeaderboardEntry(
    val rank: Int,
    val full_name: String?,
    val lifetime_points: Int,
    val total_points: Int,
    val user_id: String? = null,
    val rank_change: String? = null
)
