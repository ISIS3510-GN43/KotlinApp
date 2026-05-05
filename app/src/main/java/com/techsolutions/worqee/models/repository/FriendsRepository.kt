package com.techsolutions.worqee.models.repository

import android.util.Log
import com.techsolutions.worqee.models.clases.Metrica
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.clases.daos.AmigoDao
import com.techsolutions.worqee.models.network.RetrofitClient
import com.techsolutions.worqee.models.storage.toAmigoEntity
import com.techsolutions.worqee.models.storage.toUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FriendsRepository {

    suspend fun getAmigos(
        userId: String,
        amigoDao: AmigoDao
    ): Result<Pair<List<Usuario>, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getAmigos(userId)

                if (response.isSuccessful) {
                    val lista = response.body()

                    if (lista != null) {
                        val amigos = lista.map { Usuario.fromMap(it) }

                        amigoDao.deleteAll()
                        amigoDao.insertAll(amigos.map { it.toAmigoEntity() })

                        Result.success(Pair(amigos, false))
                    } else {
                        Result.failure(Exception("Lista de amigos vacía"))
                    }
                } else {
                    cargarAmigosDesdeCache(amigoDao)
                }

            } catch (e: Exception) {
                cargarAmigosDesdeCache(amigoDao)
            }
        }
    }

    private suspend fun cargarAmigosDesdeCache(
        amigoDao: AmigoDao
    ): Result<Pair<List<Usuario>, Boolean>> {
        return try {
            val cached = amigoDao.getAll()

            if (cached.isNotEmpty()) {
                val amigos = cached.map { it.toUsuario() }
                Result.success(Pair(amigos, true))
            } else {
                Result.failure(Exception("Sin red y sin caché local de amigos"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarUsuarioPorUsername(username: String): Result<Usuario> {
        return UsuarioRepository.getUsuarioPorUsername(username)
    }

    suspend fun enviarSolicitudAmistad(
        fromId: String,
        toId: String
    ): Result<Unit> {
        return try {
            val response = RetrofitClient.apiService.enviarSolicitud(
                userId = fromId,
                amigoid = toId
            )

            if (response.isSuccessful) {
                Log.d("FriendsRepository", "Solicitud enviada de $fromId a $toId")
                Result.success(Unit)
            } else {
                Log.e("FriendsRepository", "Error HTTP ${response.code()}")
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e("FriendsRepository", "Error enviando solicitud: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun registrarMetricaSolicitud(userId: String): Result<Unit> {
        return try {
            val metrica = Metrica(
                Evento = "Create request",
                FechaActividad = getCurrentIsoDate(),
                IdUsuario = userId,
                Plataforma = "Kotlin"
            )

            val response = RetrofitClient.apiService.crearNuevaMetrica(metrica)

            if (response.isSuccessful) {
                Log.d("FriendsRepository", "Métrica enviada correctamente")
                Result.success(Unit)
            } else {
                Log.e("FriendsRepository", "Error enviando métrica")
                Result.failure(Exception("Error enviando métrica"))
            }

        } catch (e: Exception) {
            Log.e("FriendsRepository", "Error métrica: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getCurrentIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}