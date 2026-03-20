package com.techsolutions.worqee.ui.screens.friends

data class FriendsUiState(
    val searchQuery: String = "",
    val availableFriends: List<FriendUiModel> = emptyList(),
    val busyFriends: List<FriendUiModel> = emptyList(),
    val offlineFriends: List<FriendUiModel> = emptyList(),
    val commonFreeTimeResult: String? = null
)

data class FriendUiModel(
    val id: String,
    val name: String,
    val avatarUrl: String = "",
    val status: FriendStatus,
    val freeAtLabel: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

enum class FriendStatus {
    AVAILABLE, BUSY, OFFLINE
}