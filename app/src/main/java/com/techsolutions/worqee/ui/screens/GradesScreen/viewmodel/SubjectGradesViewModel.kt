package com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubjectGradesViewModel(
    private val materia: Materia
) : ViewModel() {
    private val _materiaState = MutableStateFlow(materia.copy(notas = materia.notas.toMutableList()))
    val materiaState: StateFlow<Materia> = _materiaState.asStateFlow()

    fun getMateria(): Materia = materia

    fun agregarActividad(nombre: String, nota: Float, porcentaje: Float) {
        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )
        materia.notas.add(nuevaNota)
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
    }

    fun actualizarObjetivo(objetivo: String) {
        val objValue = objetivo.toDoubleOrNull() ?: 0.0
        materia.objetivo = objValue
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
    }
    fun calcularPromedio(): Float = materia.calcularPromedio()
    fun calcularProgreso(): Float = materia.calcularProgreso()
}
