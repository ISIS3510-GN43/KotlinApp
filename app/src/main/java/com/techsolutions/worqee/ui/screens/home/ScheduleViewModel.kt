package com.techsolutions.worqee.ui.screens.home

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Dia
import com.techsolutions.worqee.models.Usuario
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
        // Protegemos el acceso al singleton — si aún no está listo, salimos sin crashear
        val usuario = try {
            Usuario.getInstance()
        } catch (e: IllegalStateException) {
            return
        }

        val horarioActivo = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()

        val materias = horarioActivo?.materias ?: emptyList()
        val diasDisponibles = materias
            .flatMap { it.dias }
            .distinct()
            .sortedBy { it.ordinal }

        _uiState.value = ScheduleUiState(
            titulo = horarioActivo?.titulo ?: "My Schedule",
            allMaterias = materias,
            availableDays = diasDisponibles,
            selectedDay = diasDisponibles.firstOrNull(),
            viewMode = ScheduleViewMode.DAY
        )
    }

    fun onDaySelected(day: Dia) {
        _uiState.value = _uiState.value.copy(selectedDay = day)
    }

    fun toggleViewMode() {
        val newMode = if (_uiState.value.viewMode == ScheduleViewMode.DAY) {
            ScheduleViewMode.WEEK
        } else {
            ScheduleViewMode.DAY
        }
        _uiState.value = _uiState.value.copy(viewMode = newMode)
    }
}