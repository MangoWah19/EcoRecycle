package com.example.fyp1.api

data class PointsData(
    val events: List<BackendPointsEvent> = emptyList(),
    val total: Int = 0,
    val lifetimeTotal: Int = 0
)

data class BackendPointsEvent(
    val id: String,
    val userId: String,
    val missionId: String? = null,
    val submissionId: String? = null,
    val recyclingSubmissionId: String? = null,
    val redemptionId: String? = null,
    val points: Int,
    val eventType: String,
    val status: String,
    val approvedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
