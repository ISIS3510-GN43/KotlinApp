package com.techsolutions.worqee.models.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.techsolutions.worqee.models.clases.User

object LocalStorageManager {

    private const val TAG = "LocalStorage"
    private const val PREFS_NAME = "worqee_prefs"
    private const val USER_KEY = "usuario_data"
    private const val USER_ID_KEY = "userId"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d(TAG, "LocalStorageManager initialized")
    }

    fun saveUserId(userId: String) {
        prefs.edit {
            putString(USER_ID_KEY, userId)
        }
        Log.d(TAG, "User ID saved in session")
    }

    fun loadUserId(): String? {
        return prefs.getString(USER_ID_KEY, null)
    }

    fun clearSession() {
        prefs.edit {
            remove(USER_ID_KEY)
            remove(USER_KEY)
        }
        Log.d(TAG, "Session cleared")
    }

    fun saveUser(user: User) {
        try {
            val json = gson.toJson(user)

            prefs.edit {
                putString(USER_KEY, json)
            }

            val subjectCount = user.schedules.sumOf { it.subjects.size }
            Log.d(TAG, "User saved to cache - $subjectCount subjects")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user: ${e.message}", e)
        }
    }

    fun loadUser(): User? {
        return try {
            val json = prefs.getString(USER_KEY, null)

            if (json != null) {
                val user = gson.fromJson(json, User::class.java)
                val subjectCount = user.schedules.sumOf { it.subjects.size }

                Log.d(TAG, "User loaded from cache - $subjectCount subjects")
                user
            } else {
                Log.d(TAG, "No cached user found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user: ${e.message}", e)
            null
        }
    }

    fun clearUser() {
        prefs.edit {
            remove(USER_KEY)
        }
        Log.d(TAG, "User removed from cache")
    }

    fun clearCache() {
        prefs.edit {
            clear()
        }
        Log.d(TAG, "Cache cleared")
    }
}