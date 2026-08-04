package com.example.fyp1.api

import okhttp3.MultipartBody
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST

interface RecyclingApiService {
    @Multipart
    @POST("uploads/recycling-proof")
    suspend fun uploadRecyclingProof(
        @Part file: MultipartBody.Part
    ): Response<ApiEnvelope<UploadData>>

    @POST("recycling/submissions")
    suspend fun createSubmission(
        @Body request: CreateRecyclingSubmissionRequest
    ): Response<ApiEnvelope<RecyclingSubmissionData>>

    @POST("recycling/qr/claim")
    suspend fun claimQr(
        @Body request: JsonObject
    ): Response<ApiEnvelope<RecyclingSubmissionData>>

    @GET("recycling/submissions/me")
    suspend fun getMySubmissions(): Response<ApiEnvelope<RecyclingSubmissionsData>>

    @GET("recycling/point-rates")
    suspend fun getPointRates(): Response<ApiEnvelope<PointRatesData>>
}
