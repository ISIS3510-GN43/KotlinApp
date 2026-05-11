package com.techsolutions.worqee.models.clases

class User(
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var password: String = "",
    var birthday: String = "",
    var friendsIds: MutableList<String> = mutableListOf(),
    var friendsUsernames: MutableList<String> = mutableListOf(),
    var schedules: MutableList<Schedule> = mutableListOf(),
    var requests: MutableList<String> = mutableListOf(),
    var events: MutableList<Any?> = mutableListOf(),
    var photo: String = ""
) {

    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "gmail" to email,
            "username" to username,
            "password" to password,
            "cumpleanios" to birthday,
            "amigosIds" to friendsIds,
            "amigosUsernames" to friendsUsernames,
            "horarios" to schedules.map { it.toJson() },
            "solicitudes" to requests,
            "eventos" to events,
            "foto" to photo
        )
    }

    fun updateSubject(updatedSubject: Subject) {
        for (schedule in schedules) {
            for (i in schedule.subjects.indices) {
                if (schedule.subjects[i].id == updatedSubject.id) {
                    schedule.subjects[i] = updatedSubject
                    return
                }
            }
        }
    }

    companion object {
        private var instance: User? = null

        fun getInstance(): User {
            return instance ?: throw IllegalStateException("Usuario no inicializado")
        }

        fun isInitialized(): Boolean {
            return instance != null
        }

        fun setInstance(user: User) {
            instance = user
        }

        fun clearInstance() {
            instance = null
        }

        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                email = map["gmail"] as? String ?: "",
                username = map["username"] as? String ?: "",
                password = map["password"] as? String ?: "",
                birthday = map["cumpleanios"] as? String ?: "",
                friendsIds = ((map["amigosIds"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                friendsUsernames = ((map["amigosUsernames"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                schedules = ((map["horarios"] as? List<*>)?.mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    Schedule.fromJson(it as Map<String, Any?>)
                } ?: emptyList<Schedule>()).toMutableList(),
                requests = ((map["solicitudes"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList<String>()).toMutableList(),
                events = ((map["eventos"] as? List<*>) ?: emptyList<Any?>()).toMutableList(),
                photo = map["foto"] as? String ?: ""
            )
        }
    }
}