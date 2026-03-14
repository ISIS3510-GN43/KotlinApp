package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesViewModel : ViewModel() {
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materiasState: StateFlow<List<Materia>> = _materias.asStateFlow()
    init {
        loadMaterias()
    }
    fun loadMaterias() {
        val usuario = Usuario.getInstance()
        val horarioActivo = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()
        val materiasCopia = horarioActivo?.materias?.map { it.copy(notas = it.notas.toMutableList()) } ?: emptyList()
        
        _materias.value = materiasCopia
    }
    fun refresh() {
        loadMaterias()
    }
}