package com.techsolutions.worqee.models.clases

import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Subject(
    var id: String = "",
    var name: String = "",
    var classrooms: MutableList<String> = mutableListOf(),
    var days: MutableList<Day> = mutableListOf(),
    var startHours: MutableList<Int> = mutableListOf(),
    var endHours: MutableList<Int> = mutableListOf(),
    var color: String = "",
    var startDate: LocalDateTime = LocalDateTime.now(),
    var endDate: LocalDateTime = LocalDateTime.now(),
    var grades: MutableList<Grade> = mutableListOf(),
    var professor: String = "",
    var objective: Double = 0.0
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to name,
            "aula" to classrooms,
            "dias" to days.map { it.toJson() },
            "horaInicio" to startHours,
            "horaFin" to endHours,
            "color" to color,
            "fechaInicio" to startDate.toString(),
            "fechaFin" to endDate.toString(),
            "notas" to grades.map { it.toJson() },
            "profesor" to professor,
            "objetivo" to objective
        )
    }

    fun calculateAverage(): Float {
        if (grades.isEmpty()) return 0f

        var sum = 0f

        grades.forEach { grade ->
            sum += grade.value.toFloat() * (grade.percentage.toFloat() / 100f)
        }

        return sum
    }

    fun calculateProgress(): Float {
        val average = calculateAverage()

        return if (objective > 0) {
            (average / objective.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    fun isAtRisk(): Boolean {
        val addedPercentage = grades.sumOf { it.percentage }
        val average = calculateAverage()

        return addedPercentage >= 30 && average < 3.0
    }

    fun getAddedPercentage(): Double {
        return grades.sumOf { it.percentage }
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Subject {
            return Subject(
                id = json["id"] as? String ?: "",
                name = json["nombre"] as? String ?: "",
                classrooms = ((json["aula"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList()).toMutableList(),
                days = ((json["dias"] as? List<*>)?.mapNotNull { it as? String }?.map {
                    Day.fromJson(it)
                } ?: emptyList()).toMutableList(),
                startHours = ((json["horaInicio"] as? List<*>)?.mapNotNull {
                    (it as? Number)?.toInt()
                } ?: emptyList()).toMutableList(),
                endHours = ((json["horaFin"] as? List<*>)?.mapNotNull {
                    (it as? Number)?.toInt()
                } ?: emptyList()).toMutableList(),
                color = json["color"] as? String ?: "",
                startDate = parseDateTime(json["fechaInicio"] as? String),
                endDate = parseDateTime(json["fechaFin"] as? String),
                grades = ((json["notas"] as? List<*>)?.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    Grade.fromJson(it as? Map<String, Any?> ?: return@mapNotNull null)
                } ?: emptyList()).toMutableList(),
                professor = json["profesor"] as? String ?: "",
                objective = (json["objetivo"] as? Number)?.toDouble() ?: 0.0
            )
        }

        private fun parseDateTime(value: String?): LocalDateTime {
            if (value.isNullOrBlank()) return LocalDateTime.now()

            return runCatching {
                LocalDateTime.parse(value)
            }.getOrElse {
                runCatching {
                    ZonedDateTime.parse(value).toLocalDateTime()
                }.getOrDefault(LocalDateTime.now())
            }
        }
    }
}