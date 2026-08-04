package com.example.fyp1.api

data class ApiEnvelope<T>(
    val data: T
)

data class ApiErrorEnvelope(
    val error: ApiErrorBody?
)

data class ApiErrorBody(
    val message: String?
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val name: String
)

data class AuthData(
    val token: String,
    val user: AuthUser
)

data class MeData(
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
