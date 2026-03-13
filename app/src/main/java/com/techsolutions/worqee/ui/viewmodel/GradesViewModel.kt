package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario

class GradesViewModel : ViewModel() {

    fun obtenerMaterias(): List<Materia> {

        val usuario = Usuario.getInstance()

        val horarioActivo = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()

        return horarioActivo?.materias ?: emptyList()
    }
}