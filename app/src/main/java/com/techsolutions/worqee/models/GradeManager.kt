package com.techsolutions.worqee.models

class GradeManager {
    private val actividades = mutableListOf<ActivityGrade>()
    private var goal: GradeGoal? = null
    fun setGoal(objetivo: Float) {
        goal = GradeGoal(objetivo)
    }
    fun getGoal(): Float? {
        return goal?.objetivo
    }
    fun addActivity(nombre: String, nota: Float, porcentaje: Float) {
        actividades.add(ActivityGrade(nombre, nota, porcentaje))
    }
    fun getActivities(): List<ActivityGrade> {
        return actividades
    }
    fun calcularPromedioPonderado(): Float {
        if (actividades.isEmpty()) return 0f
        var suma = 0f
        for (a in actividades) {
            suma += a.nota * (a.porcentaje / 100f)
        }
        return suma
    }
}