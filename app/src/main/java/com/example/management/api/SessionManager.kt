package com.example.management.api

import android.content.Context
import android.content.SharedPreferences
import com.example.management.data.model.UserSummary
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "careplus_clinic_prefs"
        private const val KEY_TOKEN = "key_auth_token"
        private const val KEY_USER = "key_user_info"
        private const val KEY_BASE_URL = "key_base_url"
        
        // Mặc định: IP Wi-Fi máy tính hiện tại (192.168.0.100)
        const val DEFAULT_BASE_URL = "http://192.168.0.100:8080/"
    }

    var baseUrl: String
        get() {
            val saved = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
            // Tự động chuyển đổi nếu máy từng lưu IP cũ 192.168.0.101
            return if (saved.contains("192.168.0.101")) {
                val updated = saved.replace("192.168.0.101", "192.168.0.100")
                prefs.edit().putString(KEY_BASE_URL, updated).apply()
                updated
            } else {
                saved
            }
        }
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_BASE_URL, formatted).apply()
        }

    fun saveAuthToken(token: String?) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUser(user: UserSummary?) {
        if (user == null) {
            prefs.edit().remove(KEY_USER).apply()
        } else {
            val json = gson.toJson(user)
            prefs.edit().putString(KEY_USER, json).apply()
        }
    }

    fun getUser(): UserSummary? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, UserSummary::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    fun logout() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply()
    }
}
