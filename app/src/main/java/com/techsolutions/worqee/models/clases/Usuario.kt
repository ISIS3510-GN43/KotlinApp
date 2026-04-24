package com.techsolutions.worqee.models.clases

class Usuario(
    var id: String = "",
    var gmail: String = "",
    var username: String = "",
    var password: String = "",
    var cumpleanios: String = "",
    var amigosIds: MutableList<String> = mutableListOf(),
    var amigosUsernames: MutableList<String> = mutableListOf(),
    var horarios: MutableList<Horario> = mutableListOf(),
    var solicitudes: MutableList<String> = mutableListOf(),
    var eventos: MutableList<Any?> = mutableListOf(),
    var foto: String = ""
) {

    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "gmail" to gmail,
            "username" to username,
            "password" to password,
            "cumpleanios" to cumpleanios,
            "amigosIds" to amigosIds,
            "amigosUsernames" to amigosUsernames,
            "horarios" to horarios.map { it.toJson() },
            "solicitudes" to solicitudes,
            "eventos" to eventos,
            "foto" to foto
        )
    }

    fun actualizarMateria(materiaActualizada: Materia) {
        for (horario in horarios) {
            for (i in horario.materias.indices) {
                if (horario.materias[i].id == materiaActualizada.id) {
                    horario.materias[i] = materiaActualizada
                    return
                }
            }
        }
    }

    companion object {
        private var instance: Usuario? = null

        fun getInstance(): Usuario {
            return instance ?: throw IllegalStateException("Usuario no inicializado")
        }

        fun isInitialized(): Boolean {
            return instance != null
        }

        fun setInstance(usuario: Usuario) {
            instance = usuario
        }

        fun clearInstance() {
            instance = null
        }

        fun fromMap(map: Map<String, Any?>): Usuario {
            return Usuario(
                id = map["id"] as? String ?: "",
                gmail = map["gmail"] as? String ?: "",
                username = map["username"] as? String ?: "",
                password = map["password"] as? String ?: "",
                cumpleanios = map["cumpleanios"] as? String ?: "",
                amigosIds = ((map["amigosIds"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                amigosUsernames = ((map["amigosUsernames"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                horarios = ((map["horarios"] as? List<*>)?.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    (Horario.fromJson(it as Map<String, Any?>))
                } ?: emptyList<Horario>()).toMutableList(),
                solicitudes = ((map["solicitudes"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                eventos = ((map["eventos"] as? List<*>) ?: emptyList<Any?>()).toMutableList(),
                foto = map["foto"] as? String ?: ""
            )
        }
    }
}