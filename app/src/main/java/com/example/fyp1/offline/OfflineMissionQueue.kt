package com.example.fyp1.offline

import android.content.Context
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.AuthSessionManager
import com.example.fyp1.api.BackendSubmission
import java.io.File

data class MissionProofToQueue(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)

object OfflineMissionQueue {
    suspend fun queuePendingSubmission(
        context: Context,
        missionId: String,
        proofText: String,
        quantity: Int?,
        proof: MissionProofToQueue
    ): AuthResult<BackendSubmission> {
        val appContext = context.applicationContext
        val user = AuthSessionManager(appContext).getUser()
            ?: return AuthResult.Error("Please log in before saving a pending mission proof.")

        val localId = "LOCAL-MSUB-${System.currentTimeMillis()}"
        val proofFile = saveProofFile(appContext, localId, proof)
        val entity = PendingMissionSubmissionEntity(
            localId = localId,
            missionId = missionId,
            userId = user.id,
            proofText = proofText,
            quantity = quantity,
            localImagePath = proofFile.absolutePath,
            mimeType = proof.mimeType,
            fileName = proof.fileName
        )

        OfflineDatabase.get(appContext).offlineDao().upsertPendingMissionSubmission(entity)
        OfflineWorkManager.enqueuePendingMissionUploads(appContext)
        return AuthResult.Success(entity.toBackendSubmission())
    }

    private fun saveProofFile(context: Context, localId: String, proof: MissionProofToQueue): File {
        val directory = File(context.filesDir, "pending_uploads/mission_proofs").apply {
            mkdirs()
        }
        val safeName = proof.fileName
            .replace(Regex("""[^A-Za-z0-9._-]"""), "-")
            .ifBlank { "mission-proof.jpg" }
        return File(directory, "$localId-$safeName").also { file ->
            file.writeBytes(proof.bytes)
        }
    }
}
