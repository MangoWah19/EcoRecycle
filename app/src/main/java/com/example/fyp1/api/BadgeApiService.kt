package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.GET

interface BadgeApiService {
    @GET("badges/progress")
    suspend fun getBadgeProgress(): Response<ApiEnvelope<BadgeProgressData>>
}
