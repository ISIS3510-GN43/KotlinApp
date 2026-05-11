package com.techsolutions.worqee.models.storage

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class PendingAction(
    val type: String,

    @SerializedName(value = "subjectId", alternate = ["materiaId"])
    val subjectId: String,

    @SerializedName(value = "subjectName", alternate = ["materiaNombre"])
    val subjectName: String,

    @SerializedName(value = "gradeTitle", alternate = ["notaTitulo"])
    val gradeTitle: String,

    @SerializedName(value = "gradeValue", alternate = ["notaGrade"])
    val gradeValue: Double,

    @SerializedName(value = "gradePercentage", alternate = ["notaPorcentaje"])
    val gradePercentage: Double,

    val timestampMs: Long = System.currentTimeMillis()
)

object PendingSyncManager {

    private const val TAG = "PendingSync"
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

        Log.d(TAG, "Pending action saved: ${action.type} - ${action.gradeTitle}")
    }

    fun getPendingActions(): List<PendingAction> {
        return try {
            val json = getPrefs().getString(KEY_ACTIONS, null) ?: return emptyList()
            val type = object : TypeToken<List<PendingAction>>() {}.type

            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading pending actions: ${e.message}")
            emptyList()
        }
    }

    fun clearPendingActions() {
        getPrefs().edit().remove(KEY_ACTIONS).apply()
        Log.d(TAG, "Pending actions cleared")
    }

    fun hasPendingActions(): Boolean {
        return getPendingActions().isNotEmpty()
    }

    private fun saveActions(actions: List<PendingAction>) {
        getPrefs().edit().putString(KEY_ACTIONS, gson.toJson(actions)).apply()
    }
}