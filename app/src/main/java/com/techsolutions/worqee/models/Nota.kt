package com.techsolutions.worqee.models

data class Nota(
    var grade: Double,
    var porcentaje: Double,
    var titulo: String? = null
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "grade" to grade,
            "porcentaje" to porcentaje,
            "titulo" to titulo
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Nota {
            return Nota(
                grade = (json["grade"] as Number).toDouble(),
                porcentaje = (json["porcentaje"] as Number).toDouble(),
                titulo = json["titulo"] as? String
            )
        }
    }
}