package com.techsolutions.worqee.models.clases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amigos")
data class FriendEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String,
    val password: String,
    val birthday: String,
    val friendsIdsJson: String,
    val friendsUsernamesJson: String,
    val schedulesJson: String,
    val requestsJson: String
    // Photo intentionally excluded
)