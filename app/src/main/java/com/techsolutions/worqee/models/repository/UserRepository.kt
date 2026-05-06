package com.techsolutions.worqee.models.repository
import android.util.Log
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.network.RetrofitClient
import com.techsolutions.worqee.models.storage.LocalStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
object UserRepository {

    suspend fun register(usuario: Usuario): Result<Usuario> {
        return try {
            val response = RetrofitClient.apiService.register(usuario)

            if (response.isSuccessful) {
                val usuarioCreado = response.body()

                if (usuarioCreado != null) {
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

    suspend fun cargarUsuarioDesdeServidor(userId: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getUsuario(userId)

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        Result.success(Usuario.fromMap(data))
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP ${response.code()}"))
                }

            } catch (e: Exception) {
                Log.e("UsuarioRepository", "Error cargando usuario: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun obtenerUidPorUsername(username: String): String? {
        return try {
            val response = RetrofitClient.apiService.getUidByUsername(username)

            if (response.isSuccessful) {
                response.body()?.string()?.trim()
            } else {
                Log.e("UsuarioRepository", "Error buscando UID: ${response.code()}")
                null
            }

        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error buscando UID: ${e.message}", e)
            null
        }
    }

    suspend fun getUsuarioPorUsername(username: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val uid = obtenerUidPorUsername(username)
                    ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

                val response = RetrofitClient.apiService.getUsuario(uid)

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        Result.success(Usuario.fromMap(data))
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error HTTP ${response.code()}"))
                }

            } catch (e: Exception) {
                Log.e("UsuarioRepository", "Error obteniendo usuario: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /*
     * Alias temporal para no romper código existente.
     * En realidad este método recibe username, no ID.
     * Luego se debería reemplazar por getUsuarioPorUsername().
     */
    suspend fun getUsuarioPorId(username: String): Result<Usuario> {
        return getUsuarioPorUsername(username)
    }

    fun guardarEnCaché(usuario: Usuario) {
        try {
            LocalStorageManager.guardarUsuario(usuario)
            Log.d("UsuarioRepository", "Usuario guardado en caché")
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error guardando usuario en caché: ${e.message}", e)
        }
    }

    fun cargarDelCaché(): Usuario? {
        return try {
            val usuario = LocalStorageManager.cargarUsuario()

            if (usuario != null) {
                Log.d("UsuarioRepository", "Usuario cargado desde caché")
            } else {
                Log.d("UsuarioRepository", "No hay usuario en caché")
            }

            usuario
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error cargando usuario desde caché: ${e.message}", e)
            null
        }
    }

    fun limpiarCaché() {
        try {
            LocalStorageManager.limpiarUsuario()
            Log.d("UsuarioRepository", "Caché de usuario limpiado")
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error limpiando caché: ${e.message}", e)
        }
    }
}