package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LeaderboardApiService {
    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("timeframe") timeframe: String
    ): Response<ApiEnvelope<BackendLeaderboardData>>
}
