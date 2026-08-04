package com.example.fyp1

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: Long? = null,
    val user_id: String,
    val achievement_type: String,
    val unlocked_at: String? = null
)

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
