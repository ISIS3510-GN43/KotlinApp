package com.techsolutions.worqee.models.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techsolutions.worqee.models.clases.Horario
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.clases.entities.AmigoEntity

private val gson = Gson()

fun Usuario.toAmigoEntity(): AmigoEntity {
    return AmigoEntity(
        id = id,
        gmail = gmail,
        username = username,
        password = password,
        cumpleanios = cumpleanios,
        amigosIdsJson = gson.toJson(amigosIds),
        amigosUsernamesJson = gson.toJson(amigosUsernames),
        horariosJson = gson.toJson(horarios.map { it.toJson() }),
        solicitudesJson = gson.toJson(solicitudes)
    )
}

fun AmigoEntity.toUsuario(): Usuario {
    val listType = object : TypeToken<List<String>>() {}.type
    val horariosRawType = object : TypeToken<List<Map<String, Any?>>>() {}.type

    val horariosRaw: List<Map<String, Any?>> = gson.fromJson(horariosJson, horariosRawType)

    return Usuario(
        id = id,
        gmail = gmail,
        username = username,
        password = password,
        cumpleanios = cumpleanios,
        amigosIds = gson.fromJson(amigosIdsJson, listType),
        amigosUsernames = gson.fromJson(amigosUsernamesJson, listType),
        horarios = horariosRaw.map { Horario.fromJson(it) }.toMutableList(),
        solicitudes = gson.fromJson(solicitudesJson, listType),
        foto = "" // excluida intencionalmente
    )
}