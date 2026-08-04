package com.example.fyp1

import kotlinx.serialization.Serializable

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
