package com.techsolutions.worqee.models.clases

enum class Dia {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    fun toJson(): String {
        return when (this) {
            MIERCOLES -> "MIÉRCOLES"
            SABADO -> "SÁBADO"
            else -> name
        }
    }

    companion object {
        fun fromJson(value: String): Dia {
            val upper = value.uppercase()

            return when {
                upper == "LUNES" -> LUNES
                upper == "MARTES" -> MARTES
                upper == "MIERCOLES" || upper == "MIÉRCOLES" -> MIERCOLES
                upper == "JUEVES" -> JUEVES
                upper == "VIERNES" -> VIERNES
                upper == "SABADO" || upper == "SÁBADO" -> SABADO
                upper == "DOMINGO" -> DOMINGO
                upper.endsWith("COLES") -> MIERCOLES
                upper.endsWith("BADO") -> SABADO
                else -> throw IllegalArgumentException("Día inválido: $value")
            }
        }
    }
}