package com.techsolutions.worqee.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.clases.Materia
import com.techsolutions.worqee.models.clases.Nota
import com.techsolutions.worqee.models.repository.GradeOperationResult
import com.techsolutions.worqee.models.repository.GradesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectGradesViewModel(
    private val materia: Materia,
    private val context: Context
) : ViewModel() {

    private val _materiaState =
        MutableStateFlow(materia.copy(notas = materia.notas.toMutableList()))
    val materiaState: StateFlow<Materia> = _materiaState.asStateFlow()

    private val _estáEnRiesgo = MutableStateFlow(materia.estáEnRiesgo())
    val estáEnRiesgo: StateFlow<Boolean> = _estáEnRiesgo.asStateFlow()

    private val _porcentajeAgregado =
        MutableStateFlow(materia.obtenerPorcentajeAgregado())
    val porcentajeAgregado: StateFlow<Double> = _porcentajeAgregado.asStateFlow()

    private val _isOffline =
        MutableStateFlow(GradesRepository.isOffline(context))
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _hasPendingSync =
        MutableStateFlow(GradesRepository.hasPendingSync())
    val hasPendingSync: StateFlow<Boolean> = _hasPendingSync.asStateFlow()

    init {
        syncPendingActions()
    }

    fun getMateria(): Materia = materia

    fun agregarActividad(
        nombre: String,
        nota: Float,
        porcentaje: Float
    ) {
        val result = GradesRepository.agregarActividad(
            context = context,
            materia = materia,
            nombre = nombre,
            nota = nota,
            porcentaje = porcentaje
        )

        aplicarResultado(result)
        actualizarMateriaState()
    }

    fun eliminarActividad(nota: Nota) {
        val result = GradesRepository.eliminarActividad(
            context = context,
            materia = materia,
            nota = nota
        )

        aplicarResultado(result)
        actualizarMateriaState()
    }

    fun actualizarObjetivo(objetivo: String) {
        val objValue = objetivo.toDoubleOrNull() ?: 0.0

        val result = GradesRepository.actualizarObjetivo(
            context = context,
            materia = materia,
            objetivo = objValue
        )

        aplicarResultado(result)
        actualizarMateriaState()
    }

    fun syncPendingActions() {
        viewModelScope.launch {
            val result = GradesRepository.syncPendingActions(context)
            aplicarResultado(result)
        }
    }

    private fun aplicarResultado(result: GradeOperationResult) {
        when (result) {
            is GradeOperationResult.Success -> {
                _isOffline.value = result.isOffline
                _hasPendingSync.value = result.hasPendingSync
            }

            is GradeOperationResult.Error -> {
                // Luego podemos agregar un errorState si quieren mostrar mensajes.
            }
        }
    }

    private fun actualizarMateriaState() {
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
        _estáEnRiesgo.value = materia.estáEnRiesgo()
        _porcentajeAgregado.value = materia.obtenerPorcentajeAgregado()
    }

    fun calcularPromedio(): Float = materia.calcularPromedio()

    fun calcularProgreso(): Float = materia.calcularProgreso()
}