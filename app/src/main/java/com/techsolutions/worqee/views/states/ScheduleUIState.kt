package com.techsolutions.worqee.views.states

import com.techsolutions.worqee.models.clases.Day
import com.techsolutions.worqee.models.clases.Subject

enum class ScheduleViewMode {
    DAY, WEEK
}

data class ScheduleUiState(
    val title: String = "Mi horario",
    val allSubjects: List<Subject> = emptyList(),
    val availableDays: List<Day> = emptyList(),
    val selectedDay: Day? = null,
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAY
) {
    val filteredSubjects: List<Subject>
        get() = if (selectedDay == null) {
            emptyList()
        } else {
            allSubjects
                .filter { subject -> subject.days.contains(selectedDay) }
                .sortedBy { it.startHours.firstOrNull() ?: Int.MAX_VALUE }
        }
}