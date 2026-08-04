package com.example.fyp1.offline

import android.content.Context
import com.example.fyp1.api.AuthResult
import com.example.fyp1.api.AuthSessionManager
import com.example.fyp1.api.ContentRepository
import com.example.fyp1.api.MissionRepository
import com.example.fyp1.api.PointsRepository
import com.example.fyp1.api.SubmitMissionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

object OfflineWorkManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var uploadRunning = false
    @Volatile private var syncRunning = false

    fun enqueuePendingMissionUploads(context: Context) {
        val appContext = context.applicationContext
        if (uploadRunning) return
        uploadRunning = true
        scope.launch {
            try {
                uploadPendingMissionSubmissions(appContext)
            } finally {
                uploadRunning = false
            }
        }
    }

    fun enqueueSync(context: Context) {
        val appContext = context.applicationContext
        if (syncRunning) return
        syncRunning = true
        scope.launch {
            try {
                refreshCaches(appContext)
            } finally {
                syncRunning = false
            }
        }
    }

    private suspend fun uploadPendingMissionSubmissions(context: Context) {
        if (!AuthSessionManager(context).isLoggedIn()) return

        val dao = OfflineDatabase.get(context).offlineDao()
        val pendingSubmissions = dao.getUploadableMissionSubmissions()
        if (pendingSubmissions.isEmpty()) return

        val repository = MissionRepository(context)
        pendingSubmissions.forEach { pending ->
            dao.updatePendingMissionStatus(
                localId = pending.localId,
                status = PendingMissionStatus.Uploading.value,
                retryCount = pending.retryCount,
                errorMessage = null
            )

            val file = File(pending.localImagePath)
            if (!file.exists()) {
                dao.updatePendingMissionStatus(
                    localId = pending.localId,
                    status = PendingMissionStatus.Failed.value,
                    retryCount = pending.retryCount + 1,
                    errorMessage = "The saved proof image could not be found."
                )
                return@forEach
            }

            val upload = when (val result = repository.uploadMissionProof(file.readBytes(), pending.mimeType, pending.fileName)) {
                is AuthResult.Success -> result.value
                is AuthResult.Error -> {
                    dao.updatePendingMissionStatus(
                        localId = pending.localId,
                        status = PendingMissionStatus.Failed.value,
                        retryCount = pending.retryCount + 1,
                        errorMessage = result.message
                    )
                    return@forEach
                }
            }

            val request = SubmitMissionRequest(
                proofText = pending.proofText,
                proofImageUrl = upload.fileUrl,
                quantity = pending.quantity,
                uploadId = upload.id
            )

            when (val result = repository.submitMission(pending.missionId, request)) {
                is AuthResult.Success -> {
                    dao.upsertSubmission(result.value.toCachedEntity())
                    dao.deletePendingMissionSubmission(pending.localId)
                    runCatching { file.delete() }
                }
                is AuthResult.Error -> {
                    dao.updatePendingMissionStatus(
                        localId = pending.localId,
                        status = PendingMissionStatus.Failed.value,
                        retryCount = pending.retryCount + 1,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun refreshCaches(context: Context) {
        if (!AuthSessionManager(context).isLoggedIn()) return
        val missionRepository = MissionRepository(context)
        val contentRepository = ContentRepository(context)
        val pointsRepository = PointsRepository(context)
        missionRepository.getMissions()
        missionRepository.getMySubmissions()
        contentRepository.getContent()
        pointsRepository.getMyPoints()
    }
}
