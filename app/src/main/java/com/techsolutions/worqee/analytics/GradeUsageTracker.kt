package com.techsolutions.worqee.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.techsolutions.worqee.models.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GradeUsageTracker {

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    fun trackGradeAdded(materiaId: String, materiaNombre: String, notaTitulo: String) {
        val userId = try { Usuario.getInstance().id } catch (e: Exception) { "unknown" }
        val event = mapOf(
            "event_type"     to "grade_added",
            "user_id"        to userId,
            "materia_id"     to materiaId,
            "materia_nombre" to materiaNombre,
            "nota_titulo"    to notaTitulo,
            "timestamp"      to dateFormat.format(Date()),
            "timestamp_ms"   to System.currentTimeMillis()
        )
        db.collection("grade_usage_events").add(event)
            .addOnSuccessListener { Log.d("GradeTracker", "Firestore OK") }
            .addOnFailureListener { e -> Log.e("GradeTracker", "Error: ${e.message}") }

        val bundle = Bundle().apply {
            putString("materia_nombre", materiaNombre)
            putString("nota_titulo", notaTitulo)
            putString("user_id", userId)
        }
        analytics?.logEvent("grade_recorded", bundle)
        Log.d("GradeTracker", "Analytics event: grade_recorded")
    }
    fun trackObjectiveUpdated(materiaId: String, materiaNombre: String, nuevoObjetivo: Double) {
        val userId = try { Usuario.getInstance().id } catch (e: Exception) { "unknown" }
        val event = mapOf(
            "event_type"      to "objective_updated",
            "user_id"         to userId,
            "materia_id"      to materiaId,
            "materia_nombre"  to materiaNombre,
            "nuevo_objetivo"  to nuevoObjetivo,
            "timestamp"       to dateFormat.format(Date()),
            "timestamp_ms"    to System.currentTimeMillis()
        )
        db.collection("grade_usage_events").add(event)
            .addOnSuccessListener { Log.d("GradeTracker", "Firestore OK") }
            .addOnFailureListener { e -> Log.e("GradeTracker", "Error: ${e.message}") }
        val bundle = Bundle().apply {
            putString("materia_nombre", materiaNombre)
            putDouble("nuevo_objetivo", nuevoObjetivo)
            putString("user_id", userId)
        }
        analytics?.logEvent("grade_objective_updated", bundle)
        Log.d("GradeTracker", "Analytics event: grade_objective_updated")
    }
}