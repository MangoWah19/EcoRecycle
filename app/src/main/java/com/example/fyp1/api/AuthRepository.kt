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

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val sessionManager = AuthSessionManager(appContext)
    private val gson = Gson()

    private val api: AuthApiService by lazy {
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
            .create(AuthApiService::class.java)
    }

    suspend fun register(name: String, email: String, password: String): AuthResult<AuthData> =
        runCatching {
            handleAuthResponse(api.register(RegisterRequest(name = name, email = email, password = password)))
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }

    suspend fun login(email: String, password: String): AuthResult<AuthData> {
        val result = runCatching {
            handleAuthResponse(api.login(LoginRequest(email = email, password = password)))
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }
        if (result is AuthResult.Success) {
            sessionManager.saveSession(result.value.token, result.value.user)
        }
        return result
    }

    suspend fun me(): AuthResult<AuthUser> {
        val response = runCatching { api.me() }.getOrElse {
            return AuthResult.Error(networkErrorMessage(it))
        }
        val result = runCatching {
            handleResponse(response) { it.data.user }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }
        if (result is AuthResult.Success) {
            sessionManager.saveUser(result.value)
        } else if (response.code() == 401) {
            sessionManager.clearSession()
        }
        return result
    }

    suspend fun updateProfileName(name: String): AuthResult<AuthUser> {
        val response = runCatching { api.updateMe(UpdateProfileRequest(name = name)) }.getOrElse {
            return AuthResult.Error(networkErrorMessage(it))
        }
        val result = runCatching {
            handleResponse(response) { it.data.user }
        }.getOrElse { AuthResult.Error(networkErrorMessage(it)) }
        if (result is AuthResult.Success) {
            sessionManager.saveUser(result.value)
        } else if (response.code() == 401) {
            sessionManager.clearSession()
        }
        return result
    }

    fun getSavedUser(): AuthUser? = sessionManager.getUser()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun clearSession() {
        sessionManager.clearSession()
    }

    private fun authInterceptor(): Interceptor = Interceptor { chain ->
        val token = sessionManager.getToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private fun handleAuthResponse(response: Response<ApiEnvelope<AuthData>>): AuthResult<AuthData> =
        handleResponse(response) { it.data }

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
            400 -> "Missing or invalid parameters."
            401 -> "Invalid email or password."
            409 -> "An account with this email already exists."
            in 500..599 -> "Server error. Please try again later."
            else -> "Request failed. Please try again."
        }

        return runCatching {
            response.errorBody()?.string()?.let { raw ->
                gson.fromJson(raw, ApiErrorEnvelope::class.java)?.error?.message
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun networkErrorMessage(error: Throwable): String {
        return when (error) {
            is java.net.ConnectException -> "Connection Error: We could not reach EcoRecycle services. Please check your internet connection and try again."
            is java.net.SocketTimeoutException -> "Connection Error: EcoRecycle services are taking too long to respond. Please try again shortly."
            is java.net.UnknownHostException -> "Connection Error: We could not connect to EcoRecycle services. Please check your internet connection."
            else -> "Unexpected Error: ${error.localizedMessage ?: "Please try again."}"
        }
    }
}

sealed class AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}
