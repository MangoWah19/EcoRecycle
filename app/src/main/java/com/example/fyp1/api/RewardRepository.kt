package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.Redemption
import com.example.fyp1.Reward
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RewardRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val gson = Gson()

    private val api: RewardApiService by lazy {
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
            .create(RewardApiService::class.java)
    }

    suspend fun getRewards(): AuthResult<List<Reward>> =
        runCatching {
            handleResponse(api.getRewards()) { envelope ->
                envelope.data.rewards.map { it.toReward() }
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun getMyRedemptions(): AuthResult<List<Redemption>> =
        runCatching {
            handleResponse(api.getMyRedemptions()) { envelope ->
                envelope.data.redemptions.map { it.toRedemption() }
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun redeemReward(id: String, quantity: Int): AuthResult<Redemption> =
        runCatching {
            handleResponse(api.redeemReward(id, RedeemRewardRequest(quantity))) {
                it.data.redemption.toRedemption()
            }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    private fun BackendReward.toReward(): Reward =
        Reward(
            id = id,
            name = name,
            points_required = pointsRequired,
            stock = stock,
            image_url = imageUrl,
            category = category,
            expires_at = expiresAt,
            is_active = isActive,
            created_at = createdAt
        )

    private fun BackendRedemption.toRedemption(): Redemption =
        Redemption(
            id = id,
            user_id = userId,
            reward_id = rewardId,
            item_name = itemName,
            quantity = quantity,
            points_spent = pointsSpent,
            created_at = createdAt,
            status = status,
            reserved_at = reservedAt,
            claimed_at = claimedAt,
            completed_at = completedAt,
            cancelled_at = cancelledAt,
            cancel_reason = cancelReason,
            expires_at = expiresAt
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
            400 -> "Reward cannot be redeemed."
            401 -> "Please log in again."
            403 -> "You do not have permission for this action."
            409 -> "Reward is out of stock."
            429 -> "Reward is on cooldown."
            in 500..599 -> "Server error. Please try again later."
            else -> "Could not load reward data."
        }

        return runCatching {
            response.errorBody()?.string()?.let { raw ->
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
}
