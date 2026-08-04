package com.example.fyp1.api

import android.content.Context
import com.example.fyp1.offline.OfflineDatabase
import com.example.fyp1.offline.toBackendContent
import com.example.fyp1.offline.toCachedEntity
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ContentRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val offlineDao = OfflineDatabase.get(appContext).offlineDao()
    private val gson = Gson()

    private val api: ContentApiService by lazy {
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
            .create(ContentApiService::class.java)
    }

    suspend fun getContent(): AuthResult<List<BackendContent>> =
        runCatching {
            when (val result = handleResponse(api.getContent()) { it.data.content }) {
                is AuthResult.Success -> {
                    offlineDao.upsertContent(result.value.map { it.toCachedEntity() })
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedContentOrError(networkErrorMessage(it)) }

    suspend fun getContentById(id: String): AuthResult<BackendContent> =
        runCatching {
            when (val result = handleResponse(api.getContentById(id)) { it.data.content }) {
                is AuthResult.Success -> {
                    offlineDao.upsertContentItem(result.value.toCachedEntity())
                    result
                }
                is AuthResult.Error -> result
            }
        }.getOrElse { cachedContentItemOrError(id, networkErrorMessage(it)) }

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
            404 -> "Content not found."
            in 500..599 -> "Server error. Please try again later."
            else -> "Could not load content."
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

    private suspend fun cachedContentOrError(error: String): AuthResult<List<BackendContent>> {
        val cached = offlineDao.getCachedContent().map { it.toBackendContent() }
        return if (cached.isNotEmpty()) AuthResult.Success(cached) else AuthResult.Error(error)
    }

    private suspend fun cachedContentItemOrError(id: String, error: String): AuthResult<BackendContent> {
        val cached = offlineDao.getCachedContentById(id)?.toBackendContent()
        return if (cached != null) AuthResult.Success(cached) else AuthResult.Error(error)
    }
}
