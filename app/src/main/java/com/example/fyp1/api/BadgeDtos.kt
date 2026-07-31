package com.example.fyp1.api

data class BadgeProgressData(
    val earned: List<BackendBadgeProgress> = emptyList(),
    val locked: List<BackendBadgeProgress> = emptyList()
)

data class BackendBadgeProgress(
    val badgeId: String,
    val slug: String,
    val name: String,
    val description: String,
    val tier: String,
    val criteriaType: String,
    val criteriaValue: Int,
    val currentProgress: Int,
    val progressPercentage: Int,
    val status: String,
    val awardedAt: String? = null
)
