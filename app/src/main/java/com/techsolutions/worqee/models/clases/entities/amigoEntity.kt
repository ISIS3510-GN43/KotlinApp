package com.techsolutions.worqee.models.clases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amigos")
data class AmigoEntity(
    @PrimaryKey
    val id: String,
    val gmail: String,
    val username: String,
    val password: String,
    val cumpleanios: String,
    val amigosIdsJson: String,       // List<String> serializada
    val amigosUsernamesJson: String, // List<String> serializada
    val horariosJson: String,        // List<Horario> serializada — objeto complejo
    val solicitudesJson: String      // List<String> serializada
    // foto excluida intencionalmente
)