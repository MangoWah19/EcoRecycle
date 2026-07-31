package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ContentApiService {
    @GET("content")
    suspend fun getContent(): Response<ApiEnvelope<ContentListData>>

    @GET("content/{id}")
    suspend fun getContentById(@Path("id") id: String): Response<ApiEnvelope<ContentData>>
}
