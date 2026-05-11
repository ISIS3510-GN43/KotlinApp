package com.techsolutions.worqee.models.repository

import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.storage.LocalStorageManager

object SessionRepository {

    suspend fun restoreSession(): User? {
        val userId = LocalStorageManager.loadUserId() ?: return null

        val cachedUser = UserRepository.loadFromCache()

        if (cachedUser != null) {
            User.setInstance(cachedUser)
            return cachedUser
        }

        val serverResult = UserRepository.loadUserFromServer(userId)

        if (serverResult.isSuccess) {
            val user = serverResult.getOrThrow()

            User.setInstance(user)
            UserRepository.saveToCache(user)

            return user
        }

        return null
    }

    fun getCurrentUser(): User? {
        return try {
            User.getInstance()
        } catch (_: IllegalStateException) {
            val cachedUser = UserRepository.loadFromCache()

            if (cachedUser != null) {
                User.setInstance(cachedUser)
            }

            cachedUser
        }
    }

    suspend fun clearSession() {
        AuthRepository.logout()
    }
}