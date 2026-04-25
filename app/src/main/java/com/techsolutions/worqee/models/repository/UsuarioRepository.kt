package com.techsolutions.worqee.models.repository

import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.network.RetrofitClient
import com.techsolutions.worqee.models.storage.LocalStorageManager
import android.util.Log
import com.techsolutions.worqee.models.clases.daos.AmigoDao
import com.techsolutions.worqee.models.clases.entities.AmigoEntity
import com.techsolutions.worqee.models.storage.toAmigoEntity
import com.techsolutions.worqee.models.storage.toUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            val response = RetrofitClient.apiService.getUsuario(userId)

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


    //Sprint 3 - EC, cache, local storage

    suspend fun getAmigos(userId: String, amigoDao: AmigoDao): Result<List<Usuario>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getAmigos(userId)
                if (response.isSuccessful) {
                    val lista = response.body()
                    if (lista != null) {
                        val usuarios = lista.map { Usuario.fromMap(it) }
                        // Red exitosa → actualizamos caché
                        amigoDao.deleteAll()
                        amigoDao.insertAll(usuarios.map { it.toAmigoEntity() })
                        Result.success(usuarios)
                    } else {
                        Result.failure(Exception("Lista vacía"))
                    }
                } else {
                    // Red falló → intentamos caché
                    Log.w(
                        "UsuarioRepository",
                        "Red falló (${response.code()}), cargando desde Room"
                    )
                    val cached = amigoDao.getAll()
                    if (cached.isNotEmpty()) {
                        Result.success(cached.map { it.toUsuario() })
                    } else {
                        Result.failure(Exception("Sin red y sin caché"))
                    }
                }
            } catch (e: Exception) {
                // Sin conexión → intentamos caché
                Log.w("UsuarioRepository", "Sin conexión, cargando desde Room")
                try {
                    val cached = amigoDao.getAll()
                    if (cached.isNotEmpty()) {
                        Result.success(cached.map { it.toUsuario() })
                    } else {
                        Result.failure(Exception("Sin red y sin caché"))
                    }
                } catch (dbEx: Exception) {
                    Result.failure(dbEx)
                }
            }
        }
    }

    // MÉTODOS DE CACHÉ LOCAL
    fun guardarEnCaché(usuario: Usuario) {
        try {
            LocalStorageManager.guardarUsuario(usuario)
            Log.d("UsuarioRepository", "Usuario guardado en caché")
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error al guardar en caché: ${e.message}", e)
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
            Log.e("UsuarioRepository", "Error al cargar del caché: ${e.message}", e)
            null
        }
    }

    fun limpiarCaché() {
        try {
            LocalStorageManager.limpiarCaché()
            Log.d("UsuarioRepository", "Caché limpiado")
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error al limpiar caché: ${e.message}", e)
        }
    }

    // Sprint 3

    suspend fun obtenerUidPorUsername(username: String): String? {
        return try {
            val response = RetrofitClient.apiService.getUidByUsername(username)

            if (response.isSuccessful) {
                response.body()?.string()?.trim()
            } else {
                Log.e("UsuarioAPI", "Error: ${response.code()}")
                null
            }

        } catch (e: Exception) {
            Log.e("UsuarioAPI", "Error en la conexión: ${e.message}", e)
            null
        }
    }

    suspend fun getUsuarioPorId(username: String): Result<Usuario> {
        return withContext(Dispatchers.IO) {
            try {
                val idResponse = RetrofitClient.apiService.getUidByUsername(username)
                if (!idResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("Usuario no encontrado"))
                }
                val uid = idResponse.body()?.string()?.trim()
                    ?: return@withContext Result.failure(Exception("UID vacío"))

                val response = RetrofitClient.apiService.getUsuario(uid)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) Result.success(Usuario.fromMap(data))
                    else Result.failure(Exception("Respuesta vacía"))
                } else {
                    Result.failure(Exception("Error HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("UsuarioAPI", "Error obteniendo usuario por id: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun enviarSolicitudAmistad(fromId: String, toId: String): Result<Unit> {
        return try {
            val response = RetrofitClient.apiService.enviarSolicitud(
                userId = fromId,
                amigoid = toId
            )
            if (response.isSuccessful) {
                Log.d("UsuarioAPI", "Solicitud enviada de $fromId a $toId")
                Result.success(Unit)
            } else {
                Log.e("UsuarioAPI", "Error al enviar solicitud: ${response.code()}")
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UsuarioAPI", "Error enviando solicitud: ${e.message}", e)
            Result.failure(e)
        }
    }

}