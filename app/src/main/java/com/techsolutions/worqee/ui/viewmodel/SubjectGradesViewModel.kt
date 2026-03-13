package com.techsolutions.worqee.viewmodel

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubjectGradesViewModel(
    private val materia: Materia
) : ViewModel() {

    private val _materiaState = MutableStateFlow(materia)
    val materiaState: StateFlow<Materia> = _materiaState

    fun getMateria(): Materia = materia

    fun agregarActividad(nombre: String, nota: Float, porcentaje: Float) {
        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )
        materia.notas.add(nuevaNota)
        // reemitir la misma instancia para notificar cambios
        _materiaState.value = materia
    }

    fun actualizarObjetivo(objetivo: String) {
        materia.objetivo = objetivo.toDoubleOrNull() ?: 0.0
        _materiaState.value = materia
    }
    fun calcularPromedio(): Float = materia.calcularPromedio()
    fun calcularProgreso(): Float = materia.calcularProgreso()
}