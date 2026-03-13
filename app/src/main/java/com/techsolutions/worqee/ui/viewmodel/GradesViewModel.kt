package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario

class GradesViewModel : ViewModel() {

    fun obtenerMaterias(): List<Materia> {

        val usuario = Usuario.getInstance()

        val horarioActivo = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()

        return horarioActivo?.materias ?: emptyList()
    }

    fun calcularPromedio(materia: Materia): Float {

        if (materia.notas.isEmpty()) return 0f

        var suma = 0f

        materia.notas.forEach {
            suma += it.grade.toFloat() * (it.porcentaje.toFloat() / 100f)
        }

        return suma
    }

    fun calcularProgreso(materia: Materia): Float {

        val promedio = calcularPromedio(materia)

        return if (materia.objetivo > 0) {
            (promedio / materia.objetivo.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }
}