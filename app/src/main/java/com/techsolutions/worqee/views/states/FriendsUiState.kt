package com.techsolutions.worqee.views.states

data class FriendsUiState(
    val searchQuery: String = "",
    val availableFriends: List<FriendUiModel> = emptyList(),
    val busyFriends: List<FriendUiModel> = emptyList(),
    val offlineFriends: List<FriendUiModel> = emptyList(),
    val commonFreeTimeResult: String? = null,
    // --- NUEVO ---
    val showAddFriendDialog: Boolean = false,
    val addFriendUsername: String = "",
    val addFriendSearchStatus: AddFriendSearchStatus = AddFriendSearchStatus.IDLE,
    val foundUser: FoundUserUiModel? = null,
    val sendRequestStatus: SendRequestStatus = SendRequestStatus.IDLE
)

enum class AddFriendSearchStatus { IDLE, LOADING, SUCCESS, NOT_FOUND, ERROR }
enum class SendRequestStatus { IDLE, LOADING, SUCCESS, ERROR }

data class FoundUserUiModel(
    val uid: String,
    val username: String,
    val foto: String
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