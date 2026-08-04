package com.example.fyp1

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class RecyclingLog(
    val id: String? = null,
    val user_id: String,
    val source: String? = null,
    val qr_code_id: String? = null,
    val material_type: String,
    val quantity: Double,
    val status: String,
    val points_awarded: Int,
    val created_at: String? = null,
    val is_duplicate_flagged: Boolean = false,
    val admin_notes: String? = null,
    val proof_image_url: String? = null
)

data class MaterialGuide(
    val name: String,
    val points: String,
    val color: Color,
    val icon: ImageVector
)
