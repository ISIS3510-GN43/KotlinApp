package com.techsolutions.worqee.models.repository

import com.google.firebase.auth.FirebaseAuth
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.storage.LocalStorageManager
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("No se pudo obtener el ID del usuario autenticado"))

            LocalStorageManager.saveUserId(userId)

            val userResult = UserRepository.loadUserFromServer(userId)

            if (userResult.isSuccess) {
                val user = userResult.getOrThrow()

                User.setInstance(user)
                UserRepository.saveToCache(user)

                Result.success(user)
            } else {
                Result.failure(
                    userResult.exceptionOrNull()
                        ?: Exception("No se pudo cargar el usuario desde el servidor")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        birthday: String
    ): Result<User> {
        return try {
            val defaultPhotoUrl =
                "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fotos%20de%20perfil%2Fuser_profile_photo.png?alt=media&token=63c45849-4d40-4805-a222-0e130c588bc8"

            val newUser = User(
                email = email,
                password = password,
                username = username,
                birthday = birthday,
                photo = defaultPhotoUrl
            )

            val result = UserRepository.register(newUser)

            if (result.isSuccess) {
                val createdUser = result.getOrThrow()

                LocalStorageManager.saveUserId(createdUser.id)
                User.setInstance(createdUser)
                UserRepository.saveToCache(createdUser)

                Result.success(createdUser)
            } else {
                Result.failure(
                    result.exceptionOrNull()
                        ?: Exception("Error al registrar usuario")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()

        User.clearInstance()
        LocalStorageManager.clearSession()
        UserRepository.clearCache()
    }
}