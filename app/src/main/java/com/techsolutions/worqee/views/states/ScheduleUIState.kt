package com.techsolutions.worqee.views.states

import com.techsolutions.worqee.models.clases.Dia
import com.techsolutions.worqee.models.clases.Materia

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