package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiEnvelope<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthData>>

    @GET("auth/me")
    suspend fun me(): Response<ApiEnvelope<MeData>>

    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): Response<ApiEnvelope<MeData>>
}
