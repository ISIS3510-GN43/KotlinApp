package com.techsolutions.worqee.models.clases

data class Schedule(
    var id: String = "",
    var title: String = "",
    var firstDay: Day? = null,
    var lastDay: Day? = null,
    var wallpaper: String = "",
    var subjects: MutableList<Subject> = mutableListOf(),
    var isActive: Boolean = false
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "titulo" to title,
            "primerDia" to firstDay?.toJson(),
            "ultimoDia" to lastDay?.toJson(),
            "fondoPantalla" to wallpaper,
            "materias" to subjects.map { it.toJson() },
            "activo" to isActive
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Schedule {
            val subjectsData = json["materias"] as? List<*>
                ?: json["clases"] as? List<*>
                ?: emptyList<Any?>()

            return Schedule(
                id = json["id"] as? String ?: "",
                title = json["titulo"] as? String ?: "",
                firstDay = (json["primerDia"] as? String)?.let { Day.fromJson(it) },
                lastDay = (json["ultimoDia"] as? String)?.let { Day.fromJson(it) },
                wallpaper = json["fondoPantalla"] as? String ?: "",
                subjects = subjectsData.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    Subject.fromJson(it as? Map<String, Any?> ?: return@mapNotNull null)
                }.toMutableList(),
                isActive = json["activo"] as? Boolean ?: false
            )
        }
    }
}