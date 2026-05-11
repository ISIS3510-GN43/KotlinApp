package com.techsolutions.worqee.models.repository

import com.techsolutions.worqee.models.clases.Schedule
import com.techsolutions.worqee.models.clases.Subject

object ScheduleRepository {

    fun getActiveSchedule(): Schedule? {
        val user = SessionRepository.getCurrentUser() ?: return null

        return user.schedules.firstOrNull { it.isActive }
            ?: user.schedules.firstOrNull()
    }

    fun getActiveSubjects(): List<Subject> {
        return getActiveSchedule()?.subjects ?: emptyList()
    }
}