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
                    Exception(mapAuthError(userResult.exceptionOrNull()))
                )
            }

        } catch (e: Exception) {
            Result.failure(Exception(mapAuthError(e)))
        }
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        birthday: String
    ): Result<User> {
        return try {
            val fotoDefault = "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fotos%20de%20perfil%2Fuser_profile_photo.png?alt=media&token=63c45849-4d40-4805-a222-0e130c588bc8"

            val newUser = User(
                email    = email,
                password = password,   // el backend lo necesita para crear en Firebase
                username = username,
                birthday = birthday,
                photo    = fotoDefault
                // id vacío — el backend lo asigna al crear en Firebase
            )

            val result = UserRepository.register(newUser)

            if (result.isSuccess) {
                val createdUser = result.getOrThrow()

                // Ahora sí hacer signIn con Firebase en el cliente para obtener la sesión
                val authResult = firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .await()

                val userId = authResult.user?.uid
                    ?: return Result.failure(Exception("No se pudo obtener la sesión tras el registro"))

                LocalStorageManager.saveUserId(userId)
                User.setInstance(createdUser)
                UserRepository.saveToCache(createdUser)

                Result.success(createdUser)
            } else {
                Result.failure(Exception(mapAuthError(result.exceptionOrNull())))
            }

        } catch (e: Exception) {
            Result.failure(Exception(mapAuthError(e)))
        }
    }
    suspend fun logout() {
        firebaseAuth.signOut()
        User.clearInstance()
        LocalStorageManager.clearSession()
        UserRepository.clearCache()
    }

    private fun mapAuthError(error: Throwable?): String {
        val message = error?.message.orEmpty()

        return when {
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    message.contains("wrong-password", ignoreCase = true) ||
                    message.contains("INVALID_PASSWORD", ignoreCase = true) ->
                "Correo o contraseña incorrectos"

            message.contains("user-not-found", ignoreCase = true) ||
                    message.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "No existe una cuenta con ese correo"

            message.contains("email-already-in-use", ignoreCase = true) ||
                    message.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "Este correo ya tiene una cuenta registrada"

            message.contains("weak-password", ignoreCase = true) ->
                "La contraseña es demasiado débil"

            message.contains("invalid-email", ignoreCase = true) ->
                "El formato del correo no es válido"

            message.contains("user-disabled", ignoreCase = true) ->
                "Esta cuenta ha sido deshabilitada. Contacta al soporte"

            message.contains("too-many-requests", ignoreCase = true) ||
                    message.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ->
                "Demasiados intentos. Espera unos minutos antes de intentar de nuevo"

            message.contains("network", ignoreCase = true) ||
                    message.contains("unable to resolve host", ignoreCase = true) ||
                    message.contains("failed to connect", ignoreCase = true) ->
                "Sin conexión a internet. Verifica tu red e intenta de nuevo"

            message.contains("409") ->
                "Ese nombre de usuario ya está en uso"

            message.contains("404") ->
                "No se encontraron los datos del usuario. Contacta al soporte"

            message.contains("The email address is already registered", ignoreCase = true)
                -> "Este correo ya tiene una cuenta registrada"

            message.contains("500") ||
                    message.contains("503") ->
                "Error del servidor. Intenta más tarde"



            else ->
                "Ocurrió un error inesperado. Intenta de nuevo"
        }
    }
}