package com.techsolutions.worqee.models.storage

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PendingAction(
    val type: String,       
    val materiaId: String,
    val materiaNombre: String,
    val notaTitulo: String,
    val notaGrade: Double,
    val notaPorcentaje: Double,
    val timestampMs: Long = System.currentTimeMillis()
)

object PendingSyncManager {

    private const val PREFS_NAME = "worqee_pending_sync"
    private const val KEY_ACTIONS = "pending_actions"
    private val gson = Gson()
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun addPendingAction(action: PendingAction) {
        val actions = getPendingActions().toMutableList()
        actions.add(action)
        saveActions(actions)
        Log.d("PendingSync", "Acción pendiente guardada: ${action.type} - ${action.notaTitulo}")
    }

    fun getPendingActions(): List<PendingAction> {
        return try {
            val json = getPrefs().getString(KEY_ACTIONS, null) ?: return emptyList()
            val type = object : TypeToken<List<PendingAction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("PendingSync", "Error leyendo acciones pendientes: ${e.message}")
            emptyList()
        }
    }

    fun clearPendingActions() {
        getPrefs().edit().remove(KEY_ACTIONS).apply()
        Log.d("PendingSync", "Acciones pendientes limpiadas")
    }

    fun hasPendingActions(): Boolean = getPendingActions().isNotEmpty()

    private fun saveActions(actions: List<PendingAction>) {
        getPrefs().edit().putString(KEY_ACTIONS, gson.toJson(actions)).apply()
    }
}