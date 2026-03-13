package com.techsolutions.worqee.repository

import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota

class GradeRepository {

    fun agregarActividad(
        materia: Materia,
        nombre: String,
        nota: Float,
        porcentaje: Float
    ) {

        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )

        materia.notas.add(nuevaNota)
    }

    fun establecerObjetivo(
        materia: Materia,
        objetivo: Float
    ) {
        materia.objetivo = objetivo.toDouble()
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
        val objetivo = materia.objetivo.toFloat()

        if (objetivo <= 0) return 0f

        return (promedio / objetivo).coerceIn(0f, 1f)
    }
}