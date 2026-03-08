package com.techsolutions.worqee.repository

import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.network.LoginRequest
import com.techsolutions.worqee.network.RetrofitClient

class UsuarioRepository {

    suspend fun login(gmail: String, password: String): Result<Usuario> {
        return try {
            val response = RetrofitClient.apiService.login(
                LoginRequest(gmail, password)
            )

            if (response.isSuccessful) {
                val usuario = response.body()
                if (usuario != null) {
                    Usuario.setInstance(usuario)
                    Result.success(usuario)
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

    suspend fun register(usuario: Usuario): Result<Usuario> {
        return try {
            val response = RetrofitClient.apiService.register(usuario)

            if (response.isSuccessful) {
                val usuarioCreado = response.body()
                if (usuarioCreado != null) {
                    Usuario.setInstance(usuarioCreado)
                    Result.success(usuarioCreado)
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

    suspend fun getUsuarioById(id: String): Result<Usuario> {
        return try {
            val response = RetrofitClient.apiService.getUsuarioById(id)

            if (response.isSuccessful) {
                val usuario = response.body()
                if (usuario != null) {
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Usuario no encontrado"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}