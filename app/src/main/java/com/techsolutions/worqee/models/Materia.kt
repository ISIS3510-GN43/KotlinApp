package com.techsolutions.worqee.models

import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Materia(
    var id: String = "",
    var nombre: String = "",
    var aula: MutableList<String> = mutableListOf(),
    var dias: MutableList<Dia> = mutableListOf(),
    var horaInicio: MutableList<Int> = mutableListOf(),
    var horaFin: MutableList<Int> = mutableListOf(),
    var color: String = "",
    var fechaInicio: LocalDateTime = LocalDateTime.now(),
    var fechaFin: LocalDateTime = LocalDateTime.now(),
    var notas: MutableList<Nota> = mutableListOf(),
    var profesor: String = "",
    var objetivo: Double = 0.0
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "aula" to aula,
            "dias" to dias.map { it.toJson() },
            "horaInicio" to horaInicio,
            "horaFin" to horaFin,
            "color" to color,
            "fechaInicio" to fechaInicio.toString(),
            "fechaFin" to fechaFin.toString(),
            "notas" to notas.map { it.toJson() },
            "profesor" to profesor,
            "objetivo" to objetivo
        )
    }
    fun calcularPromedio(): Float {

    if (notas.isEmpty()) return 0f

    var suma = 0f

    notas.forEach {
        suma += it.grade.toFloat() * (it.porcentaje.toFloat() / 100f)
    }

    return suma
    }

    fun calcularProgreso(): Float {

    val promedio = calcularPromedio()

    return if (objetivo > 0) {
        (promedio / objetivo.toFloat()).coerceIn(0f, 1f)
    } else 0f
    }

    fun estáEnRiesgo(): Boolean {
        val porcentajeAgregado = notas.sumOf { it.porcentaje }
        val promedio = calcularPromedio()
        return porcentajeAgregado >= 30 && promedio < 3.0
    }

    fun obtenerPorcentajeAgregado(): Double {
        return notas.sumOf { it.porcentaje }
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Materia {
            return Materia(
                id = json["id"] as? String ?: "",
                nombre = json["nombre"] as? String ?: "",
                aula = ((json["aula"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()).toMutableList(),
                dias = ((json["dias"] as? List<*>)?.mapNotNull { it as? String }?.map { Dia.fromJson(it) } ?: emptyList()).toMutableList(),
                horaInicio = ((json["horaInicio"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()).toMutableList(),
                horaFin = ((json["horaFin"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()).toMutableList(),
                color = json["color"] as? String ?: "",
                fechaInicio = (json["fechaInicio"] as? String)?.let {
                    ZonedDateTime.parse(it).toLocalDateTime()
                } ?: LocalDateTime.now(),
                fechaFin = (json["fechaFin"] as? String)?.let {
                    ZonedDateTime.parse(it).toLocalDateTime()
                } ?: LocalDateTime.now(),
                notas = ((json["notas"] as? List<*>)?.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    Nota.fromJson(it as Map<String, Any?>)
                } ?: emptyList()).toMutableList(),
                profesor = json["profesor"] as? String ?: "",
                objetivo = (json["objetivo"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}