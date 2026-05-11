package com.techsolutions.worqee.models.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.techsolutions.worqee.models.clases.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GradeUsageTracker {

    private const val TAG = "GradeTracker"

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    fun trackGradeAdded(
        subjectId: String,
        subjectName: String,
        gradeTitle: String
    ) {
        val userId = try {
            User.getInstance().id
        } catch (e: Exception) {
            "unknown"
        }

        val event = mapOf(
            "event_type" to "grade_added",
            "user_id" to userId,
            "materia_id" to subjectId,
            "materia_nombre" to subjectName,
            "nota_titulo" to gradeTitle,
            "timestamp" to dateFormat.format(Date()),
            "timestamp_ms" to System.currentTimeMillis()
        )

        db.collection("grade_usage_events").add(event)
            .addOnSuccessListener { Log.d(TAG, "Firestore OK") }
            .addOnFailureListener { e -> Log.e(TAG, "Error: ${e.message}") }

        val bundle = Bundle().apply {
            putString("materia_nombre", subjectName)
            putString("nota_titulo", gradeTitle)
            putString("user_id", userId)
        }

        analytics?.logEvent("grade_recorded", bundle)
        Log.d(TAG, "Analytics event: grade_recorded")
    }

    fun trackObjectiveUpdated(
        subjectId: String,
        subjectName: String,
        newObjective: Double
    ) {
        val userId = try {
            User.getInstance().id
        } catch (e: Exception) {
            "unknown"
        }

        val event = mapOf(
            "event_type" to "objective_updated",
            "user_id" to userId,
            "materia_id" to subjectId,
            "materia_nombre" to subjectName,
            "nuevo_objetivo" to newObjective,
            "timestamp" to dateFormat.format(Date()),
            "timestamp_ms" to System.currentTimeMillis()
        )

        db.collection("grade_usage_events").add(event)
            .addOnSuccessListener { Log.d(TAG, "Firestore OK") }
            .addOnFailureListener { e -> Log.e(TAG, "Error: ${e.message}") }

        val bundle = Bundle().apply {
            putString("materia_nombre", subjectName)
            putDouble("nuevo_objetivo", newObjective)
            putString("user_id", userId)
        }

        analytics?.logEvent("grade_objective_updated", bundle)
        Log.d(TAG, "Analytics event: grade_objective_updated")
    }
}