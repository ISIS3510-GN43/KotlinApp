package com.techsolutions.worqee.models.repository

import com.techsolutions.worqee.models.clases.Horario
import com.techsolutions.worqee.models.clases.Materia

object ScheduleRepository {

    fun getHorarioActivo(): Horario? {
        val usuario = SessionRepository.getCurrentUser() ?: return null

        return usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()
    }

    fun getMateriasActivas(): List<Materia> {
        return getHorarioActivo()?.materias ?: emptyList()
    }
}