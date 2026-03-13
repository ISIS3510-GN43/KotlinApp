package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GradesViewModel : ViewModel() {

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materiasState: StateFlow<List<Materia>> = _materias

    init {
        loadMaterias()
    }

    private fun loadMaterias() {
        val usuario = Usuario.getInstance()
        val horarioActivo = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()
        _materias.value = horarioActivo?.materias ?: emptyList()
    }

    fun refresh() {
        loadMaterias()
    }

    fun calcularProgreso(materia: Materia): Float {
        return materia.calcularProgreso()
    }
}