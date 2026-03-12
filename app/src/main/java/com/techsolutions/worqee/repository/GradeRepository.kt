package com.techsolutions.worqee.repository

import com.techsolutions.worqee.models.GradeManager

class GradeRepository {
    private val manager = GradeManager()
    fun agregarObjetivo(objetivo: Float) {
        manager.setGoal(objetivo)
    }
    fun agregarActividad(nombre: String, nota: Float, porcentaje: Float) {
        manager.addActivity(nombre, nota, porcentaje)
    }
    fun obtenerPromedio(): Float {
        return manager.calcularPromedioPonderado()
    }
    fun obtenerObjetivo(): Float? {
        return manager.getGoal()
    }
    fun obtenerActividades() = manager.getActivities()
}