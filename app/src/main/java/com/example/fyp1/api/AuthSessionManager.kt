package com.example.fyp1.api

import android.content.Context

class AuthSessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(token: String, user: AuthUser) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role)
            .apply()
    }

    fun saveUser(user: AuthUser) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    fun getUser(): AuthUser? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val role = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return AuthUser(id = id, name = name, email = email, role = role)
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "backend_auth_session"
        const val KEY_TOKEN = "jwt_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_ROLE = "user_role"
    }
}
