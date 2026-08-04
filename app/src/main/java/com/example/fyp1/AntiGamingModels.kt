package com.example.fyp1

import kotlinx.serialization.Serializable

@Serializable
data class SuspiciousActivity(
    val user_id: String,
    val activity_type: String,
    val severity: String,
    val detected_at: String,
    val details: String? = null
)

data class ValidationResult(val isValid: Boolean, val issues: List<String>)
