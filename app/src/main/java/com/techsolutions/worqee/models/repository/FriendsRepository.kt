package com.techsolutions.worqee.models.repository

import android.util.Log
import com.techsolutions.worqee.models.clases.Metric
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.clases.daos.FriendDao
import com.techsolutions.worqee.models.network.RetrofitClient
import com.techsolutions.worqee.models.storage.toFriendEntity
import com.techsolutions.worqee.models.storage.toUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FriendsRepository {

    private const val TAG = "FriendsRepository"

    suspend fun getFriends(
        userId: String,
        friendDao: FriendDao
    ): Result<Pair<List<User>, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getFriends(userId)

                if (response.isSuccessful) {
                    val list = response.body()

                    if (list != null) {
                        val friends = list.map { User.fromMap(it) }

                        friendDao.deleteAll()
                        friendDao.insertAll(friends.map { it.toFriendEntity() })

                        Result.success(Pair(friends, false))
                    } else {
                        Result.failure(Exception("Lista de amigos vacía"))
                    }
                } else {
                    loadFriendsFromCache(friendDao)
                }

            } catch (e: Exception) {
                loadFriendsFromCache(friendDao)
            }
        }
    }

    private suspend fun loadFriendsFromCache(
        friendDao: FriendDao
    ): Result<Pair<List<User>, Boolean>> {
        return try {
            val cachedFriends = friendDao.getAll()

            if (cachedFriends.isNotEmpty()) {
                val friends = cachedFriends.map { it.toUser() }
                Result.success(Pair(friends, true))
            } else {
                Result.failure(Exception("Sin red y sin caché local de amigos"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUserByUsername(username: String): Result<User> {
        return UserRepository.getUserByUsername(username)
    }

    suspend fun sendFriendRequest(
        fromId: String,
        toId: String
    ): Result<Unit> {
        return try {
            val response = RetrofitClient.apiService.sendFriendRequest(
                userId = fromId,
                friendId = toId
            )

            if (response.isSuccessful) {
                Log.d(TAG, "Friend request sent from $fromId to $toId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "HTTP error ${response.code()}")
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending friend request: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registerRequestMetric(userId: String): Result<Unit> {
        return try {
            val metric = Metric(
                event = "Create request",
                activityDate = getCurrentIsoDate(),
                userId = userId,
                platform = "Kotlin"
            )

            val response = RetrofitClient.apiService.createNewMetric(metric)

            if (response.isSuccessful) {
                Log.d(TAG, "Metric sent successfully")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error sending metric")
                Result.failure(Exception("Error enviando métrica"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Metric error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getCurrentIsoDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}