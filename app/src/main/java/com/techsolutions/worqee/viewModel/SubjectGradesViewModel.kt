package com.techsolutions.worqee.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.clases.Materia
import com.techsolutions.worqee.models.clases.Nota
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubjectGradesViewModel(
    private val materia: Materia
) : ViewModel() {
    private val _materiaState = MutableStateFlow(materia.copy(notas = materia.notas.toMutableList()))
    val materiaState: StateFlow<Materia> = _materiaState.asStateFlow()

    private val _estáEnRiesgo = MutableStateFlow(materia.estáEnRiesgo())
    val estáEnRiesgo: StateFlow<Boolean> = _estáEnRiesgo.asStateFlow()

    private val _porcentajeAgregado = MutableStateFlow(materia.obtenerPorcentajeAgregado())
    val porcentajeAgregado: StateFlow<Double> = _porcentajeAgregado.asStateFlow()

    fun getMateria(): Materia = materia

    fun agregarActividad(nombre: String, nota: Float, porcentaje: Float) {
        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )
        materia.notas.add(nuevaNota)
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
        actualizarEstadoRiesgo()
        guardarEnCaché()
        Log.d("SubjectGradesViewModel", "Actividad agregada: $nombre - Guardada en caché")
    }

    fun actualizarObjetivo(objetivo: String) {
        val objValue = objetivo.toDoubleOrNull() ?: 0.0
        materia.objetivo = objValue
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
        actualizarEstadoRiesgo()
        guardarEnCaché()
        Log.d("SubjectGradesViewModel", "Objetivo actualizado a: $objValue - Guardado en caché")
    }

    private fun actualizarEstadoRiesgo() {
        _estáEnRiesgo.value = materia.estáEnRiesgo()
        _porcentajeAgregado.value = materia.obtenerPorcentajeAgregado()
    }

    private fun guardarEnCaché() {
        try {
            val usuario = Usuario.getInstance()
            UsuarioRepository.guardarEnCaché(usuario)
        } catch (e: Exception) {
            Log.e("SubjectGradesViewModel", "Error guardando en caché: ${e.message}", e)
        }
    }

    fun calcularPromedio(): Float = materia.calcularPromedio()
    fun calcularProgreso(): Float = materia.calcularProgreso()
}
