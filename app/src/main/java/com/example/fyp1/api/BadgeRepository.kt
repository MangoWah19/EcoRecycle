package com.example.fyp1.api

import android.content.Context
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class BadgeRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val gson = Gson()

    private val api: BadgeApiService by lazy {
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
            .create(BadgeApiService::class.java)
    }

    suspend fun getBadgeProgress(): AuthResult<BadgeProgressData> =
        runCatching {
            handleResponse(api.getBadgeProgress()) { it.data }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

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
            in 500..599 -> "Server error. Please try again later."
            else -> "Could not load badges."
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
