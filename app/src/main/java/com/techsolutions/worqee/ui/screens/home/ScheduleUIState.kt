package com.techsolutions.worqee.ui.screens.home

import com.techsolutions.worqee.models.Dia
import com.techsolutions.worqee.models.Materia

enum class ScheduleViewMode {
    DAY, WEEK
}

data class ScheduleUiState(
    val titulo: String = "My Schedule",
    val allMaterias: List<Materia> = emptyList(),
    val availableDays: List<Dia> = emptyList(),
    val selectedDay: Dia? = null,
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAY
) {
    val filteredMaterias: List<Materia>
        get() = if (selectedDay == null) {
            emptyList()
        } else {
            allMaterias
                .filter { materia -> materia.dias.contains(selectedDay) }
                .sortedBy { it.horaInicio.firstOrNull() ?: Int.MAX_VALUE }
        }
}