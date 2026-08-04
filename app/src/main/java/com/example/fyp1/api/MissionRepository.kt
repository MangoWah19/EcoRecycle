package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.offline.MissionProofToQueue
import com.example.fyp1.offline.OfflineDatabase
import com.example.fyp1.offline.OfflineMissionQueue
import com.example.fyp1.offline.toBackendMission
import com.example.fyp1.offline.toBackendSubmission
import com.example.fyp1.offline.toCachedEntity
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MissionRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val offlineDao = OfflineDatabase.get(appContext).offlineDao()
    private val gson = Gson()

    private val api: MissionApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor())
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(appContext.getString(com.example.fyp1.R.string.backend_base_url))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MissionApiService::class.java)
    }

    suspend fun getMissions(): AuthResult<List<BackendMission>> =
        runCatching {
            when (val result = handleResponse(api.getMissions()) { it.data.missions }) {
                is AuthResult.Success -> {
                    offlineDao.upsertMissions(result.value.map { it.toCachedEntity() })
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedMissionsOrError(networkErrorMessage(it)) }

    suspend fun getMission(id: String): AuthResult<BackendMission> =
        runCatching {
            when (val result = handleResponse(api.getMission(id)) { it.data.mission }) {
                is AuthResult.Success -> {
                    offlineDao.upsertMission(result.value.toCachedEntity())
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedMissionOrError(id, networkErrorMessage(it)) }

    suspend fun joinMission(id: String): AuthResult<BackendSubmission> =
        runCatching {
            when (val result = handleResponse(api.joinMission(id)) { it.data.submission }) {
                is AuthResult.Success -> {
                    offlineDao.upsertSubmission(result.value.toCachedEntity())
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun uploadMissionProof(
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): AuthResult<BackendUpload> =
        runCatching {
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, body)
            handleResponse(api.uploadMissionProof(part)) { it.data.upload }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun submitMission(
        id: String,
        request: SubmitMissionRequest
    ): AuthResult<BackendSubmission> =
        runCatching {
            when (val result = handleResponse(api.submitMission(id, request)) { it.data.submission }) {
                is AuthResult.Success -> {
                    offlineDao.upsertSubmission(result.value.toCachedEntity())
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun getMySubmissions(): AuthResult<List<BackendSubmission>> =
        runCatching {
            when (val result = handleResponse(api.getMySubmissions()) { it.data.submissions }) {
                is AuthResult.Success -> {
                    offlineDao.upsertSubmissions(result.value.map { it.toCachedEntity() })
                    AuthResult.Success(withPendingSubmissions(result.value))
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedSubmissionsOrError(networkErrorMessage(it)) }

    suspend fun queuePendingMissionSubmission(
        missionId: String,
        proofText: String,
        quantity: Int?,
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): AuthResult<BackendSubmission> =
        OfflineMissionQueue.queuePendingSubmission(
            context = appContext,
            missionId = missionId,
            proofText = proofText,
            quantity = quantity,
            proof = MissionProofToQueue(bytes = bytes, mimeType = mimeType, fileName = fileName)
        )

    private fun authInterceptor(): Interceptor = Interceptor { chain ->
        val token = sessionManager.getToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private fun <T, R> handleResponse(
        response: Response<T>,
        mapper: (T) -> R
    ): AuthResult<R> {
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            AuthResult.Success(mapper(body))
        } else {
            AuthResult.Error(parseError(response))
        }
    }

    private fun parseError(response: Response<*>): String {
        val fallback = when (response.code()) {
            401 -> "Please log in again."
            404 -> "Mission not found."
            in 500..599 -> "Server error. Please try again later."
            else -> "Could not load missions."
        }

        return runCatching {
            response.errorBody()?.string()?.let { raw ->
                if (raw.contains("Cannot POST", ignoreCase = true) || raw.contains("Cannot PATCH", ignoreCase = true)) {
                    return "Backend endpoint is not available yet. Please rebuild the backend container."
                }
                gson.fromJson(raw, ApiErrorEnvelope::class.java)?.error?.message
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun networkErrorMessage(error: Throwable): String {
        return when (error) {
            is java.net.ConnectException -> "Connection Error: Could not reach the backend."
            is java.net.SocketTimeoutException -> "Connection Error: The backend took too long to respond."
            is java.net.UnknownHostException -> "Connection Error: Could not resolve backend host."
            else -> "Unexpected Error: ${error.localizedMessage ?: "Please try again."}"
        }
    }

    private suspend fun cachedMissionsOrError(error: String): AuthResult<List<BackendMission>> {
        val cached = offlineDao.getCachedMissions().map { it.toBackendMission() }
        return if (cached.isNotEmpty()) AuthResult.Success(cached) else AuthResult.Error(error)
    }

    private suspend fun cachedMissionOrError(id: String, error: String): AuthResult<BackendMission> {
        val cached = offlineDao.getCachedMission(id)?.toBackendMission()
        return if (cached != null) AuthResult.Success(cached) else AuthResult.Error(error)
    }

    private suspend fun cachedSubmissionsOrError(error: String): AuthResult<List<BackendSubmission>> {
        val cached = offlineDao.getCachedSubmissions().map { it.toBackendSubmission() }
        val combined = withPendingSubmissions(cached)
        return if (combined.isNotEmpty()) AuthResult.Success(combined) else AuthResult.Error(error)
    }

    private suspend fun withPendingSubmissions(remoteOrCached: List<BackendSubmission>): List<BackendSubmission> {
        val pending = offlineDao.getPendingMissionSubmissions().map { it.toBackendSubmission() }
        return (pending + remoteOrCached).distinctBy { it.id }
    }
}
