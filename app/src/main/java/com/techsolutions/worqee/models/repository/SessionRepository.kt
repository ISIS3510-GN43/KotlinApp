package com.techsolutions.worqee.models.repository

import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.storage.LocalStorageManager

object SessionRepository {

    suspend fun restoreSession(): Usuario? {
        val userId = LocalStorageManager.cargarUserId() ?: return null

        val cachedUser = UsuarioRepository.cargarDelCaché()

        if (cachedUser != null) {
            Usuario.setInstance(cachedUser) // temporal
            return cachedUser
        }

        val serverResult = UsuarioRepository.cargarUsuarioDesdeServidor(userId)

        if (serverResult.isSuccess) {
            val usuario = serverResult.getOrThrow()

            Usuario.setInstance(usuario) // temporal
            UsuarioRepository.guardarEnCaché(usuario)

            return usuario
        }

        return null
    }

    fun getCurrentUser(): Usuario? {
        return try {
            Usuario.getInstance()
        } catch (_: IllegalStateException) {
            val cachedUser = UsuarioRepository.cargarDelCaché()

            if (cachedUser != null) {
                Usuario.setInstance(cachedUser)
            }

            cachedUser
        }
    }

    suspend fun clearSession() {
        AuthRepository.logout()
    }
}