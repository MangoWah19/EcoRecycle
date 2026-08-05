package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.offline.OfflineDatabase
import com.example.fyp1.offline.toBackendPointsEvent
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PointsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val offlineDao = OfflineDatabase.get(appContext).offlineDao()
    private val notificationRepository = NotificationRepository(appContext)
    private val gson = Gson()

    private val api: PointsApiService by lazy {
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
            .create(PointsApiService::class.java)
    }

    suspend fun getMyPoints(): AuthResult<PointsData> =
        runCatching {
            when (val result = handleResponse(api.getMyPoints()) { it.data }) {
                is AuthResult.Success -> {
                    notificationRepository.notifyPointChanges(result.value)
                    offlineDao.replacePointLedger(result.value)
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedPointsOrError(networkErrorMessage(it)) }

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
            else -> "Could not load points."
        }

        return runCatching {
            response.errorBody()?.string()?.let { raw ->
                gson.fromJson(raw, ApiErrorEnvelope::class.java)?.error?.message
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun networkErrorMessage(error: Throwable): String {
        return when (error) {
            is java.net.ConnectException -> "Could not reach the EcoRecycle server. Please check your internet connection."
            is java.net.SocketTimeoutException -> "EcoRecycle is taking too long to respond. Please try again shortly."
            is java.net.UnknownHostException -> "Could not find the EcoRecycle server. Please check your internet connection."
            else -> "Something went wrong. Please try again."
        }
    }

    private suspend fun cachedPointsOrError(error: String): AuthResult<PointsData> {
        val balance = offlineDao.getCachedPointBalance()
        val events = offlineDao.getCachedPointEvents().map { it.toBackendPointsEvent() }
        return if (balance != null || events.isNotEmpty()) {
            AuthResult.Success(
                PointsData(
                    events = events,
                    total = balance?.total ?: 0,
                    lifetimeTotal = balance?.lifetimeTotal ?: 0
                )
            )
        } else {
            AuthResult.Error(error)
        }
    }
}
