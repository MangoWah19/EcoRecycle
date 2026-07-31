package com.example.fyp1.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizApiService {
    @GET("quizzes")
    suspend fun getQuizzes(@Query("contentId") contentId: String? = null): Response<ApiEnvelope<QuizListData>>

    @GET("quizzes/{id}")
    suspend fun getQuizById(@Path("id") id: String): Response<ApiEnvelope<QuizData>>

    @POST("quizzes/{id}/attempts")
    suspend fun submitAttempt(
        @Path("id") id: String,
        @Body request: SubmitQuizAttemptRequest
    ): Response<ApiEnvelope<QuizAttemptData>>

    @GET("progress/content/{contentId}/me")
    suspend fun getMyProgressForContent(@Path("contentId") contentId: String): Response<ApiEnvelope<ProgressData>>
}
