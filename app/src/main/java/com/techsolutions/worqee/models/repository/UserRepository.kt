package com.techsolutions.worqee.models.repository

import android.util.Log
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.network.RetrofitClient
import com.techsolutions.worqee.models.storage.LocalStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserRepository {

    private const val TAG = "UserRepository"

    suspend fun register(user: User): Result<User> {
        return try {
            val response = RetrofitClient.apiService.register(user)

            if (response.isSuccessful) {
                val createdUser = response.body()

                if (createdUser != null) {
                    Result.success(createdUser)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadUserFromServer(userId: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUser(userId)

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        Result.success(User.fromMap(data))
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP ${response.code()}"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading user: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getUidByUsername(username: String): String? {
        return try {
            val response = RetrofitClient.apiService.getUidByUsername(username)

            if (response.isSuccessful) {
                response.body()?.string()?.trim()
            } else {
                Log.e(TAG, "Error searching UID: ${response.code()}")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error searching UID: ${e.message}", e)
            null
        }
    }

    suspend fun getUserByUsername(username: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val uid = getUidByUsername(username)
                    ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

                val response = RetrofitClient.apiService.getUser(uid)

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        Result.success(User.fromMap(data))
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP ${response.code()}"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error getting user: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /*
     * Temporary alias to avoid breaking existing code.
     * This method actually receives a username, not an ID.
     * Replace its usages with getUserByUsername().
     */
    suspend fun getUserById(username: String): Result<User> {
        return getUserByUsername(username)
    }

    fun saveToCache(user: User) {
        try {
            LocalStorageManager.saveUser(user)
            Log.d(TAG, "User saved to cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to cache: ${e.message}", e)
        }
    }

    fun loadFromCache(): User? {
        return try {
            val user = LocalStorageManager.loadUser()

            if (user != null) {
                Log.d(TAG, "User loaded from cache")
            } else {
                Log.d(TAG, "No cached user found")
            }

            user
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user from cache: ${e.message}", e)
            null
        }
    }

    fun clearCache() {
        try {
            LocalStorageManager.clearUser()
            Log.d(TAG, "User cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache: ${e.message}", e)
        }
    }
}