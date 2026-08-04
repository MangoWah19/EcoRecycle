package com.example.fyp1.api

data class CreateRecyclingSubmissionRequest(
    val materialType: String,
    val quantity: Double,
    val proofImageUrl: String? = null,
    val uploadId: String? = null
)

data class RecyclingSubmissionsData(
    val submissions: List<BackendRecyclingSubmission> = emptyList()
)

data class RecyclingSubmissionData(
    val submission: BackendRecyclingSubmission
)

data class PointRatesData(
    val rates: List<BackendPointRate> = emptyList()
)

data class BackendPointRate(
    val material: String,
    val ratePerKg: Int
)

data class BackendRecyclingSubmission(
    val id: String,
    val userId: String,
    val source: String? = null,
    val qrCodeId: String? = null,
    val materialType: String,
    val quantity: Double,
    val proofImageUrl: String? = null,
    val status: String,
    val pointsAwarded: Int = 0,
    val isDuplicateFlagged: Boolean = false,
    val reviewNote: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null
)
