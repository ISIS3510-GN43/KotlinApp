package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota

class SubjectGradesViewModel(
    private val materia: Materia
) : ViewModel() {

    fun obtenerActividades(): MutableList<Nota> {
        return materia.notas
    }

    fun agregarActividad(nombre: String, nota: Float, porcentaje: Float) {

        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )

        materia.notas.add(nuevaNota)
    }

    fun actualizarObjetivo(objetivo: String) {
        materia.objetivo = objetivo.toDoubleOrNull() ?: 0.0
    }

    fun calcularPromedio(): Float {

        if (materia.notas.isEmpty()) return 0f

        var suma = 0f

        materia.notas.forEach {
            suma += it.grade.toFloat() * (it.porcentaje.toFloat() / 100f)
        }

        return suma
    }

    fun calcularProgreso(): Float {

        val promedio = calcularPromedio()

        return if (materia.objetivo > 0) {
            (promedio / materia.objetivo.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    fun getMateria(): Materia {
        return materia
    }
}