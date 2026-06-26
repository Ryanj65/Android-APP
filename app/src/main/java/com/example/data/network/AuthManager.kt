package com.example.data.network

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("autocrm_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_NAME = "auth_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        private set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        private set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var name: String?
        get() = prefs.getString(KEY_NAME, null)
        private set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        private set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    fun saveAuth(token: String, email: String, name: String) {
        this.token = token
        this.email = email
        this.name = name
        this.isLoggedIn = true
    }

    fun clearAuth() {
        prefs.edit().clear().apply()
    }
}
