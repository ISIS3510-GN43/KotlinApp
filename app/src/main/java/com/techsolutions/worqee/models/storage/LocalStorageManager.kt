package com.techsolutions.worqee.models.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.techsolutions.worqee.models.clases.Usuario

object LocalStorageManager {

    private const val PREFS_NAME = "worqee_prefs"
    private const val USER_KEY = "usuario_data"
    private const val USER_ID_KEY = "userId"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.d("LocalStorage", "LocalStorageManager inicializado")
    }

    fun guardarUserId(userId: String) {
        prefs.edit {
            putString(USER_ID_KEY, userId)
        }
        Log.d("LocalStorage", "UserId guardado en sesión")
    }

    fun cargarUserId(): String? {
        return prefs.getString(USER_ID_KEY, null)
    }

    fun limpiarSesion() {
        prefs.edit {
            remove(USER_ID_KEY)
            remove(USER_KEY)
        }
        Log.d("LocalStorage", "Sesión limpiada")
    }

    fun guardarUsuario(usuario: Usuario) {
        try {
            val json = gson.toJson(usuario)

            prefs.edit {
                putString(USER_KEY, json)
            }

            val cantidadMaterias = usuario.horarios.sumOf { it.materias.size }
            Log.d("LocalStorage", "Usuario guardado en caché - $cantidadMaterias materias")
        } catch (e: Exception) {
            Log.e("LocalStorage", "Error guardando usuario: ${e.message}", e)
        }
    }

    fun cargarUsuario(): Usuario? {
        return try {
            val json = prefs.getString(USER_KEY, null)

            if (json != null) {
                val usuario = gson.fromJson(json, Usuario::class.java)
                val cantidadMaterias = usuario.horarios.sumOf { it.materias.size }

                Log.d("LocalStorage", "Usuario cargado desde caché - $cantidadMaterias materias")
                usuario
            } else {
                Log.d("LocalStorage", "No hay usuario en caché")
                null
            }
        } catch (e: Exception) {
            Log.e("LocalStorage", "Error cargando usuario: ${e.message}", e)
            null
        }
    }

    fun limpiarUsuario() {
        prefs.edit {
            remove(USER_KEY)
        }
        Log.d("LocalStorage", "Usuario limpiado de caché")
    }

    fun limpiarCaché() {
        prefs.edit {
            clear()
        }
        Log.d("LocalStorage", "Caché limpiado")
    }
}