package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota

class SubjectGradesViewModel(
    private val materia: Materia
) : ViewModel() {

    fun getMateria(): Materia {
        return materia
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
}