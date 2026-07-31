package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.GET

interface PointsApiService {
    @GET("points/me")
    suspend fun getMyPoints(): Response<ApiEnvelope<PointsData>>
}
