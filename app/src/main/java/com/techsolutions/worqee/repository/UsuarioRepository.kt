package com.techsolutions.worqee.repository

import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.network.RetrofitClient
import android.util.Log

object UsuarioRepository {

    suspend fun login(gmail: String, password: String): Result<Usuario> {
        return try {
            val response = RetrofitClient.apiService.login(
                mapOf(
                    "gmail" to gmail,
                    "password" to password
                )
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

    suspend fun cargarSingletonUsuario(userId: String): Boolean {
        return try {
            val response = RetrofitClient.usuarioApi.getUsuario(userId)

            if (response.isSuccessful) {
                val data = response.body()

                if (data != null) {
                    Log.d("UsuarioAPI", "Respuesta: $data")
                    Usuario.clearInstance()
                    Usuario.setInstance(Usuario.fromMap(data))
                    true
                } else {
                    Log.e("UsuarioAPI", "Body vacío")
                    false
                }
            } else {
                Log.e("UsuarioAPI", "Error al cargar usuario: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("UsuarioAPI", "Error en la conexión: ${e.message}", e)
            false
        }
    }

    suspend fun getAmigos(userId: String): Result<List<Usuario>> {
        return try {
            val response = RetrofitClient.apiService.getAmigos(userId)
            if (response.isSuccessful) {
                val lista = response.body()
                if (lista != null) {
                    Result.success(lista.map { Usuario.fromMap(it) })
                } else {
                    Result.failure(Exception("Lista vacía"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}