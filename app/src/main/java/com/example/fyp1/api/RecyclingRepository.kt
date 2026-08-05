package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.RecyclingLog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RecyclingRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val notificationRepository = NotificationRepository(appContext)
    private val gson = Gson()

    private val api: RecyclingApiService by lazy {
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
            .create(RecyclingApiService::class.java)
    }

    suspend fun createSubmission(
        materialType: String,
        quantity: Double,
        proofImageUrl: String? = null,
        uploadId: String? = null
    ): AuthResult<RecyclingLog> =
        runCatching {
            val request = CreateRecyclingSubmissionRequest(
                materialType = materialType,
                quantity = quantity,
                proofImageUrl = proofImageUrl,
                uploadId = uploadId
            )
            when (val result = handleResponse(api.createSubmission(request)) { it.data.submission.toRecyclingLog() }) {
                is AuthResult.Success -> {
                    notificationRepository.recordRecyclingSubmitted(result.value)
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun uploadRecyclingProof(
        bytes: ByteArray,
        mimeType: String,
        fileName: String
    ): AuthResult<BackendUpload> =
        runCatching {
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, body)
            handleResponse(api.uploadRecyclingProof(part)) { it.data.upload }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun claimQr(rawClaimPayload: String): AuthResult<RecyclingLog> =
        runCatching {
            val request = parseQrClaimPayload(rawClaimPayload)
                ?: return AuthResult.Error("This QR code is not a valid EcoRecycle recycling QR.")
            when (val result = handleResponse(api.claimQr(request)) { it.data.submission.toRecyclingLog() }) {
                is AuthResult.Success -> {
                    notificationRepository.recordRecyclingSubmitted(result.value, qrBased = true)
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun getMySubmissions(): AuthResult<List<RecyclingLog>> =
        runCatching {
            handleResponse(api.getMySubmissions()) { envelope ->
                envelope.data.submissions.map { it.toRecyclingLog() }
            }.also { result ->
                if (result is AuthResult.Success) {
                    notificationRepository.notifyRecyclingSubmissionChanges(result.value)
                }
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun getPointRates(): AuthResult<Map<String, Int>> =
        runCatching {
            handleResponse(api.getPointRates()) { envelope ->
                envelope.data.rates.associate { it.material to it.ratePerKg }
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    private fun BackendRecyclingSubmission.toRecyclingLog(): RecyclingLog =
        RecyclingLog(
            id = id,
            user_id = userId,
            source = source,
            qr_code_id = qrCodeId,
            material_type = materialType,
            quantity = quantity,
            status = status.toUiStatus(),
            points_awarded = pointsAwarded,
            created_at = submittedAt,
            is_duplicate_flagged = isDuplicateFlagged,
            admin_notes = reviewNote,
            proof_image_url = proofImageUrl
        )

    private fun parseQrClaimPayload(rawClaimPayload: String): JsonObject? =
        runCatching {
            val root = JsonParser.parseString(rawClaimPayload).asJsonObject
            val payload = root.getAsJsonObject("payload") ?: return null
            val signature = root.get("signature")?.asString?.takeIf { it.isNotBlank() } ?: return null
            val type = payload.get("type")?.asString
            val qrId = payload.get("qrId")?.asString
            val nonce = payload.get("nonce")?.asString
            val materialType = payload.get("materialType")?.asString
            val expiresAt = payload.get("expiresAt")?.asString
            val estimatedWeightKg = payload.get("estimatedWeightKg")

            if (
                type != "recycling-deposit" ||
                qrId.isNullOrBlank() ||
                nonce.isNullOrBlank() ||
                materialType.isNullOrBlank() ||
                expiresAt.isNullOrBlank() ||
                estimatedWeightKg == null ||
                signature.isBlank()
            ) {
                return null
            }

            root
        }.getOrNull()

    private fun String.toUiStatus(): String =
        when (this) {
            "PENDING_REVIEW" -> "Pending"
            "APPROVED" -> "Approved"
            "REJECTED" -> "Rejected"
            else -> this
        }

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
            400 -> "This QR code or recycling submission is invalid. Please scan the QR generated by admin."
            401 -> "Please log in again."
            403 -> "You do not have permission for this action."
            409 -> "This QR code was already used or is no longer valid. Please ask admin to issue a new QR."
            410 -> "This QR code has expired. Please ask admin to issue a new QR."
            429 -> "Too many recycling submissions. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> "Could not process recycling submission."
        }

        return runCatching {
            response.errorBody()?.string()?.let { raw ->
                gson.fromJson(raw, ApiErrorEnvelope::class.java)?.error?.message
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun networkErrorMessage(error: Throwable): String {
        return when (error) {
            is java.net.ConnectException -> "Connection Error: Could not reach EcoRecycle services. Please check your connection."
            is java.net.SocketTimeoutException -> "Connection Error: EcoRecycle services are taking too long to respond."
            is java.net.UnknownHostException -> "Connection Error: Could not find EcoRecycle services. Please check your connection."
            else -> "Unexpected Error: ${error.localizedMessage ?: "Please try again."}"
        }
    }
}
