package com.example.filmo.utils

import android.content.Context
import android.content.SharedPreferences

class UserPrefsManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserPrefsManager? = null

        fun getInstance(context: Context): UserPrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPrefsManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        const val KEY_USER_NAME = "user_name"
        const val KEY_AVATAR_ID = "avatar_id"
        const val KEY_USER_LOCATION = "user_location"
        const val KEY_DARK_MODE = "dark_mode"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("filmo_user_prefs", Context.MODE_PRIVATE)

    // User name
    fun saveUserName(name: String) {
        sharedPrefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return sharedPrefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    // Avatar
    fun saveAvatarId(avatarId: Int) {
        sharedPrefs.edit().putInt(KEY_AVATAR_ID, avatarId).apply()
    }

    fun getAvatarId(): Int {
        return sharedPrefs.getInt(KEY_AVATAR_ID, -1)
    }

    // Location
    fun saveUserLocation(location: String) {
        sharedPrefs.edit().putString(KEY_USER_LOCATION, location).apply()
    }

    fun getUserLocation(): String {
        return sharedPrefs.getString(KEY_USER_LOCATION, "Cairo, Egypt") ?: "Cairo, Egypt"
    }

    // Theme
    fun saveDarkMode(isDarkMode: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPrefs.getBoolean(KEY_DARK_MODE, false)
    }

    // Clear all data (for logout)
    fun clearUserData() {
        sharedPrefs.edit().clear().apply()
    }
}






//UserPrefsManager