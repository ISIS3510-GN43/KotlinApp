package com.techsolutions.worqee.models.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.techsolutions.worqee.models.clases.Schedule
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.clases.entities.FriendEntity

private val gson = Gson()

fun User.toFriendEntity(): FriendEntity {
    return FriendEntity(
        id = id,
        email = email,
        username = username,
        password = password,
        birthday = birthday,
        friendsIdsJson = gson.toJson(friendsIds),
        friendsUsernamesJson = gson.toJson(friendsUsernames),
        schedulesJson = gson.toJson(schedules.map { it.toJson() }),
        requestsJson = gson.toJson(requests)
    )
}

fun FriendEntity.toUser(): User {
    val stringListType = object : TypeToken<List<String>>() {}.type
    val schedulesRawType = object : TypeToken<List<Map<String, Any?>>>() {}.type

    val schedulesRaw: List<Map<String, Any?>> = gson.fromJson(schedulesJson, schedulesRawType)
        ?: emptyList()

    return User(
        id = id,
        email = email,
        username = username,
        password = password,
        birthday = birthday,
        friendsIds = gson.fromJson(friendsIdsJson, stringListType) ?: mutableListOf(),
        friendsUsernames = gson.fromJson(friendsUsernamesJson, stringListType) ?: mutableListOf(),
        schedules = schedulesRaw.map { Schedule.fromJson(it) }.toMutableList(),
        requests = gson.fromJson(requestsJson, stringListType) ?: mutableListOf(),
        photo = "" // intentionally excluded
    )
}