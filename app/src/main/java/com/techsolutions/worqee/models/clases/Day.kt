package com.techsolutions.worqee.models.clases

import java.util.Locale

enum class Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    fun toJson(): String {
        return when (this) {
            MONDAY -> "LUNES"
            TUESDAY -> "MARTES"
            WEDNESDAY -> "MIÉRCOLES"
            THURSDAY -> "JUEVES"
            FRIDAY -> "VIERNES"
            SATURDAY -> "SÁBADO"
            SUNDAY -> "DOMINGO"
        }
    }

    fun toSpanishName(): String {
        return toJson()
            .lowercase(Locale.ROOT)
            .replaceFirstChar { it.titlecase(Locale.ROOT) }
    }

    companion object {
        fun fromJson(value: String): Day {
            val upperValue = value.trim().uppercase(Locale.ROOT)

            return when (upperValue) {
                "LUNES", "MONDAY" -> MONDAY
                "MARTES", "TUESDAY" -> TUESDAY
                "MIERCOLES", "MIÉRCOLES", "WEDNESDAY" -> WEDNESDAY
                "JUEVES", "THURSDAY" -> THURSDAY
                "VIERNES", "FRIDAY" -> FRIDAY
                "SABADO", "SÁBADO", "SATURDAY" -> SATURDAY
                "DOMINGO", "SUNDAY" -> SUNDAY
                else -> throw IllegalArgumentException("Día inválido: $value")
            }
        }
    }
}