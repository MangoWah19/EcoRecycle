package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST
import okhttp3.MultipartBody

interface MissionApiService {
    @GET("missions")
    suspend fun getMissions(): Response<ApiEnvelope<MissionListData>>

    @GET("missions/{id}")
    suspend fun getMission(@Path("id") id: String): Response<ApiEnvelope<MissionData>>

    @POST("missions/{id}/join")
    suspend fun joinMission(@Path("id") id: String): Response<ApiEnvelope<SubmissionData>>

    @POST("missions/{id}/submit")
    suspend fun submitMission(
        @Path("id") id: String,
        @Body request: SubmitMissionRequest
    ): Response<ApiEnvelope<SubmissionData>>

    @Multipart
    @POST("uploads/mission-proof")
    suspend fun uploadMissionProof(@Part file: MultipartBody.Part): Response<ApiEnvelope<UploadData>>

    @GET("submissions/me")
    suspend fun getMySubmissions(): Response<ApiEnvelope<SubmissionListData>>
}
