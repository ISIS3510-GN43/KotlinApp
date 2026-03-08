package com.techsolutions.worqee.models

data class Horario(
    var id: String = "",
    var titulo: String = "",
    var primerDia: Dia? = null,
    var ultimoDia: Dia? = null,
    var fondoPantalla: String = "",
    var materias: MutableList<Materia> = mutableListOf(),
    var activo: Boolean = false
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "titulo" to titulo,
            "fondoPantalla" to fondoPantalla,
            "primerDia" to primerDia?.toJson(),
            "ultimoDia" to ultimoDia?.toJson(),
            "materias" to materias.map { it.toJson() },
            "activo" to activo
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): Horario {
            return Horario(
                id = json["id"] as? String ?: "",
                titulo = json["titulo"] as? String ?: "",
                fondoPantalla = json["fondoPantalla"] as? String ?: "",
                primerDia = (json["primerDia"] as? String)?.let { Dia.fromJson(it) },
                ultimoDia = (json["ultimoDia"] as? String)?.let { Dia.fromJson(it) },
                materias = ((json["clases"] as? List<*>)?.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    Materia.fromJson(it as Map<String, Any?>)
                } ?: emptyList()).toMutableList(),
                activo = json["activo"] as? Boolean ?: false
            )
        }
    }
}