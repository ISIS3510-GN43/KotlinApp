package com.techsolutions.worqee.viewModel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.clases.Day
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.views.states.ScheduleUiState
import com.techsolutions.worqee.views.states.ScheduleViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScheduleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        val user = try {
            User.getInstance()
        } catch (e: IllegalStateException) {
            return
        }

        val activeSchedule = user.schedules.firstOrNull { it.isActive }
            ?: user.schedules.firstOrNull()

        val subjects = activeSchedule?.subjects ?: emptyList()

        val availableDays = subjects
            .flatMap { it.days }
            .distinct()
            .sortedBy { it.ordinal }

        _uiState.value = ScheduleUiState(
            title = activeSchedule?.title ?: "Mi horario",
            allSubjects = subjects,
            availableDays = availableDays,
            selectedDay = availableDays.firstOrNull(),
            viewMode = ScheduleViewMode.DAY
        )
    }
    fun onDaySelected(day: Day) {
        _uiState.value = _uiState.value.copy(
            selectedDay = day
        )
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(
            viewMode = if (_uiState.value.viewMode == ScheduleViewMode.DAY) {
                ScheduleViewMode.WEEK
            } else {
                ScheduleViewMode.DAY
            }
        )
    }
}