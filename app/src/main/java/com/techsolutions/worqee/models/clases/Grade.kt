package com.techsolutions.worqee.models.clases

data class Grade(
    var value: Double = 0.0,
    var percentage: Double = 0.0,
    var title: String? = null
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "grade" to value,
            "porcentaje" to percentage,
            "titulo" to title
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Grade {
            return Grade(
                value = (json["grade"] as? Number)?.toDouble() ?: 0.0,
                percentage = (json["porcentaje"] as? Number)?.toDouble() ?: 0.0,
                title = json["titulo"] as? String
            )
        }
    }
}