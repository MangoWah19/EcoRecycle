package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RewardApiService {
    @GET("rewards")
    suspend fun getRewards(): Response<ApiEnvelope<RewardsData>>

    @GET("rewards/redemptions/me")
    suspend fun getMyRedemptions(): Response<ApiEnvelope<RedemptionsData>>

    @POST("rewards/{id}/redeem")
    suspend fun redeemReward(
        @Path("id") id: String,
        @Body request: RedeemRewardRequest
    ): Response<ApiEnvelope<RedemptionData>>
}
