package com.techsolutions.worqee.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.clases.Day
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.clases.daos.FriendDao
import com.techsolutions.worqee.models.repository.FriendsRepository
import com.techsolutions.worqee.models.repository.SessionRepository
import com.techsolutions.worqee.models.storage.WorqeeDatabase
import com.techsolutions.worqee.views.states.AddFriendSearchStatus
import com.techsolutions.worqee.views.states.FoundUserUiModel
import com.techsolutions.worqee.views.states.FriendStatus
import com.techsolutions.worqee.views.states.FriendUiModel
import com.techsolutions.worqee.views.states.FriendsUiState
import com.techsolutions.worqee.views.states.SendRequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class UniversityBuilding(
    val name: String,
    val lat: Double,
    val lng: Double
)

class FriendsViewModel(
    private val friendDao: FriendDao
) : ViewModel() {

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = WorqeeDatabase.getInstance(context).friendDao()

                    @Suppress("UNCHECKED_CAST")
                    return FriendsViewModel(dao) as T
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val buildings = listOf(
        UniversityBuilding("Edificio ML (Matemáticas)", 4.60178, -74.06582),
        UniversityBuilding("Edificio W (Ingeniería)", 4.60215, -74.06618),
        UniversityBuilding("Edificio SD (Santo Domingo)", 4.60143, -74.06601),
        UniversityBuilding("Biblioteca General", 4.60165, -74.06555),
        UniversityBuilding("Edificio RGA", 4.60190, -74.06540),
        UniversityBuilding("Centro Deportivo Uniandes", 4.60250, -74.06490),
        UniversityBuilding("Edificio AU (Artes)", 4.60120, -74.06570),
        UniversityBuilding("Edificio C (Ciencias)", 4.60200, -74.06650)
    )

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val user = SessionRepository.getCurrentUser() ?: return@launch

            val result = FriendsRepository.getFriends(
                userId = user.id,
                friendDao = friendDao
            )

            if (result.isFailure) {
                Log.e(
                    "FriendsViewModel",
                    "Error loading friends: ${result.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val (friends, isOffline) = result.getOrThrow()
            val allFriends = buildFriendUiModels(friends)

            _uiState.value = _uiState.value.copy(isOffline = isOffline)
            updateState(allFriends, _uiState.value.searchQuery)
        }
    }

    private fun buildFriendUiModels(friends: List<User>): List<FriendUiModel> {
        val now = java.util.Calendar.getInstance()

        val currentDay = when (now.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> Day.MONDAY
            java.util.Calendar.TUESDAY -> Day.TUESDAY
            java.util.Calendar.WEDNESDAY -> Day.WEDNESDAY
            java.util.Calendar.THURSDAY -> Day.THURSDAY
            java.util.Calendar.FRIDAY -> Day.FRIDAY
            else -> null
        }

        val currentTime = now.get(java.util.Calendar.HOUR_OF_DAY) * 100 +
                now.get(java.util.Calendar.MINUTE)

        return friends.mapIndexed { index, friend ->
            val activeSchedule = friend.schedules.firstOrNull { it.isActive }
                ?: friend.schedules.firstOrNull()

            val status = when {
                activeSchedule == null -> FriendStatus.AVAILABLE
                currentDay == null -> FriendStatus.AVAILABLE
                else -> {
                    val isBusy = activeSchedule.subjects.any { subject ->
                        subject.days.indices.any { i ->
                            val day = subject.days.getOrNull(i)
                            val start = subject.startHours.getOrElse(i) { 0 }
                            val end = subject.endHours.getOrElse(i) { 0 }

                            day == currentDay && currentTime in start until end
                        }
                    }

                    if (isBusy) {
                        FriendStatus.BUSY
                    } else {
                        FriendStatus.AVAILABLE
                    }
                }
            }

            val freeAtLabel = if (status == FriendStatus.BUSY) {
                val nextFreeTime = activeSchedule?.subjects
                    ?.flatMap { subject ->
                        subject.days.indices.map { i ->
                            val day = subject.days.getOrNull(i)
                            val end = subject.endHours.getOrElse(i) { 0 }
                            Pair(day, end)
                        }
                    }
                    ?.filter { (day, end) ->
                        day == currentDay && end > currentTime
                    }
                    ?.minByOrNull { it.second }
                    ?.second

                if (nextFreeTime != null) {
                    "Libre a las ${formatTime(nextFreeTime)}"
                } else {
                    "Ocupado"
                }
            } else {
                null
            }

            FriendUiModel(
                id = friend.id,
                name = friend.username,
                avatarUrl = friend.photo,
                status = status,
                freeAtLabel = freeAtLabel,
                lat = 4.6097 + (index * 0.01),
                lng = -74.0817 + (index * 0.01)
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        val allFriends = getAllFriends()
        updateState(allFriends, query)
    }

    fun onMessageFriend(friendId: String) {
        // TODO: open chat with this friend
    }

    private fun getAllFriends(): List<FriendUiModel> {
        val current = _uiState.value

        return current.availableFriends +
                current.busyFriends +
                current.offlineFriends
    }

    private fun updateState(
        friends: List<FriendUiModel>,
        query: String
    ) {
        val filtered = if (query.isBlank()) {
            friends
        } else {
            friends.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            availableFriends = filtered.filter {
                it.status == FriendStatus.AVAILABLE
            },
            busyFriends = filtered.filter {
                it.status == FriendStatus.BUSY
            },
            offlineFriends = filtered.filter {
                it.status == FriendStatus.OFFLINE
            }
        )
    }

    fun buildNearestBuildingUrl(
        myLat: Double,
        myLng: Double
    ): Pair<String, String> {
        val nearestBuilding = buildings.minByOrNull { building ->
            val dLat = building.lat - myLat
            val dLng = building.lng - myLng

            sqrt(dLat * dLat + dLng * dLng)
        } ?: buildings.first()

        val url = "https://www.google.com/maps/dir/?api=1" +
                "&origin=$myLat,$myLng" +
                "&destination=${nearestBuilding.lat},${nearestBuilding.lng}" +
                "&travelmode=walking"

        return Pair(url, nearestBuilding.name)
    }

    fun onFindCommonFreeTime() {
        viewModelScope.launch {
            val user = SessionRepository.getCurrentUser() ?: return@launch

            val result = FriendsRepository.getFriends(
                userId = user.id,
                friendDao = friendDao
            )

            if (result.isFailure) {
                Log.e(
                    "FriendsViewModel",
                    "Error finding common free time: ${result.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val (friends, _) = result.getOrThrow()

            data class BusyBlock(
                val day: Day,
                val start: Int,
                val end: Int
            )

            val busyBlocks = friends.flatMap { friend ->
                val activeSchedule = friend.schedules.firstOrNull { it.isActive }
                    ?: friend.schedules.firstOrNull()

                activeSchedule?.subjects?.flatMap { subject ->
                    subject.days.indices.mapNotNull { i ->
                        val day = subject.days.getOrNull(i)
                        val start = subject.startHours.getOrElse(i) { 0 }
                        val end = subject.endHours.getOrElse(i) { 0 }

                        if (day != null) {
                            BusyBlock(
                                day = day,
                                start = start,
                                end = end
                            )
                        } else {
                            null
                        }
                    }
                } ?: emptyList()
            }

            Log.i("BusyBlocks", "$busyBlocks")

            val weekDays = listOf(
                Day.MONDAY,
                Day.TUESDAY,
                Day.WEDNESDAY,
                Day.THURSDAY,
                Day.FRIDAY
            )

            val timeSlots = (800..2000 step 100).flatMap { time ->
                weekDays.map { day ->
                    val busyCount = busyBlocks.count { block ->
                        block.day == day &&
                                block.start <= time &&
                                block.end > time
                    }

                    Triple(
                        first = day,
                        second = time,
                        third = friends.size - busyCount
                    )
                }
            }

            val bestSlot = timeSlots.maxByOrNull { it.third }

            if (bestSlot != null) {
                _uiState.value = _uiState.value.copy(
                    commonFreeTimeResult =
                        "Mejor hora: ${bestSlot.first.toSpanishName()} a las ${formatTime(bestSlot.second)} — ${bestSlot.third}/${friends.size} amigos libres"
                )
            }
        }
    }

    private fun formatTime(time: Int): String {
        val hour = time / 100
        val suffix = if (hour >= 12) "PM" else "AM"

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return "%02d:00 %s".format(hour12, suffix)
    }

    fun onOpenAddFriendDialog() {
        _uiState.value = _uiState.value.copy(
            showAddFriendDialog = true,
            addFriendUsername = "",
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onDismissAddFriendDialog() {
        _uiState.value = _uiState.value.copy(
            showAddFriendDialog = false,
            addFriendUsername = "",
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onAddFriendUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            addFriendUsername = username,
            addFriendSearchStatus = AddFriendSearchStatus.IDLE,
            foundUser = null,
            sendRequestStatus = SendRequestStatus.IDLE
        )
    }

    fun onSearchFriendByUsername() {
        val username = _uiState.value.addFriendUsername.trim()
        if (username.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                addFriendSearchStatus = AddFriendSearchStatus.LOADING
            )

            val result = FriendsRepository.searchUserByUsername(username)

            if (result.isSuccess) {
                val user = result.getOrNull()

                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        addFriendSearchStatus = AddFriendSearchStatus.SUCCESS,
                        foundUser = FoundUserUiModel(
                            uid = user.id,
                            username = user.username,
                            photo = user.photo
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        addFriendSearchStatus = AddFriendSearchStatus.NOT_FOUND
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    addFriendSearchStatus = AddFriendSearchStatus.NOT_FOUND
                )
            }
        }
    }

    fun onSendFriendRequest() {
        val foundUser = _uiState.value.foundUser ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sendRequestStatus = SendRequestStatus.LOADING
            )

            val currentUser = SessionRepository.getCurrentUser()

            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    sendRequestStatus = SendRequestStatus.ERROR
                )
                return@launch
            }

            val result = FriendsRepository.sendFriendRequest(
                fromId = currentUser.id,
                toId = foundUser.uid
            )

            Log.d("API_RESULT", result.toString())

            if (result.isSuccess) {
                FriendsRepository.registerRequestMetric(currentUser.id)

                _uiState.value = _uiState.value.copy(
                    sendRequestStatus = SendRequestStatus.SUCCESS
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    sendRequestStatus = SendRequestStatus.ERROR
                )
            }
        }
    }

    fun refresh() {
        loadFriends()
    }
}