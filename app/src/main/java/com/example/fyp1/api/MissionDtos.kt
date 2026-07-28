package com.example.fyp1.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class MissionListData(
    val missions: List<BackendMission>
)

data class MissionData(
    val mission: BackendMission
)

data class SubmissionListData(
    val submissions: List<BackendSubmission>
)

data class SubmissionData(
    val submission: BackendSubmission
)

data class UploadData(
    val upload: BackendUpload
)

data class BackendUpload(
    val id: String,
    val fileUrl: String
)

data class SubmitMissionRequest(
    val proofText: String? = null,
    val proofImageUrl: String? = null,
    val quantity: Int? = null,
    val uploadId: String? = null
)

data class BackendMission(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    @SerializedName(value = "longDescription", alternate = ["long_description"])
    val longDescription: String? = null,
    @SerializedName(value = "imageUrl", alternate = ["image_url"])
    val imageUrl: String? = null,
    val guide: JsonElement? = null,
    val targetQuantity: Int? = null,
    val targetDays: Int? = null,
    val type: String,
    val startAt: String,
    val endAt: String,
    val submissionCap: Int?,
    val points: Int,
    val autoApprove: Boolean,
    val isActive: Boolean,
    val status: String,
    val createdById: String,
    val createdAt: String,
    val updatedAt: String
)

data class MissionGuideStep(
    val step: Int,
    val title: String,
    val description: String
)

data class BackendSubmission(
    val id: String,
    val userId: String,
    val missionId: String,
    val status: String,
    val proofText: String? = null,
    val proofImageUrl: String? = null,
    val quantity: Int? = null,
    val photoUrl: String? = null,
    val note: String? = null,
    val reviewNote: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val mission: SubmissionMissionSummary? = null
)

data class SubmissionMissionSummary(
    val id: String,
    val title: String,
    val slug: String,
    val points: Int
)
