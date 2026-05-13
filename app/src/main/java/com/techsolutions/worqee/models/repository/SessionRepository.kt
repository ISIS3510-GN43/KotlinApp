package com.techsolutions.worqee.models.repository

import com.google.firebase.auth.FirebaseAuth
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.storage.LocalStorageManager

object SessionRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun restoreSession(): User? {
        val userId = LocalStorageManager.loadUserId() ?: return null

        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null || firebaseUser.uid != userId) {
            LocalStorageManager.clearSession()
            UserRepository.clearCache()
            User.clearInstance()
            return null
        }

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