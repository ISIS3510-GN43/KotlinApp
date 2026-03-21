package com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.perf.FirebasePerformance
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesViewModel : ViewModel() {

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materiasState: StateFlow<List<Materia>> = _materias.asStateFlow()

    // Mapa: nombre de materia -> nota necesaria calculada
    private val _notasNecesarias = MutableStateFlow<Map<String, Float>>(emptyMap())
    val notasNecesarias: StateFlow<Map<String, Float>> = _notasNecesarias.asStateFlow()

    val tiemposCalculo = mutableListOf<Long>()

    init {
        loadMaterias()
    }

    fun loadMaterias() {
        try {
            val usuario = Usuario.getInstance()
            val horarioActivo = usuario.horarios.firstOrNull { it.activo }
                ?: usuario.horarios.firstOrNull()
            val materiasCopia = horarioActivo?.materias?.map {
                it.copy(notas = it.notas.toMutableList())
            } ?: emptyList()
            _materias.value = materiasCopia
        } catch (e: Exception) {
            _materias.value = emptyList()
        }
    }

    fun refresh() {
        // Limpiamos las notas necesarias para que se recalculen con datos frescos
        _notasNecesarias.value = emptyMap()
        loadMaterias()
    }

    fun calcularNotaNecesaria(materia: Materia): Float {
        // Firebase Performance trace — mide este cálculo en el analytics system
        val trace = FirebasePerformance.getInstance().newTrace("calcular_nota_necesaria")
        trace.start()

        val inicio = System.currentTimeMillis()

        val notaActual = materia.notas.sumOf { it.grade * (it.porcentaje / 100.0) }.toFloat()
        val porcentajeUsado = materia.notas.sumOf { it.porcentaje }.toFloat()
        val porcentajeRestante = 100f - porcentajeUsado

        val resultado = if (porcentajeRestante <= 0f) {
            0f
        } else {
            val notaFaltante = (materia.objetivo.toFloat() - notaActual) / (porcentajeRestante / 100f)
            notaFaltante.coerceIn(0f, 5f)
        }

        val fin = System.currentTimeMillis()
        val tiempoTranscurrido = fin - inicio
        tiemposCalculo.add(tiempoTranscurrido)

        // Atributo custom: materia calculada
        trace.putAttribute("materia", materia.nombre.take(100))
        // Métrica custom: tiempo local en ms (para correlacionar con la BusinessQuestionCard)
        trace.putMetric("tiempo_ms", tiempoTranscurrido)
        // Métrica custom: 1 si supera el objetivo de 100ms, 0 si no
        trace.putMetric("supera_100ms", if (tiempoTranscurrido > 100L) 1L else 0L)

        trace.stop()

        // Actualizar StateFlow para refrescar la UI
        _notasNecesarias.value = _notasNecesarias.value + (materia.nombre to resultado)

        return resultado
    }

    fun obtenerPromedioTiempoCalculo(): Long {
        if (tiemposCalculo.isEmpty()) return 0L
        return tiemposCalculo.average().toLong()
    }

    fun superaObjetivo100ms(): Boolean {
        return obtenerPromedioTiempoCalculo() > 100L
    }
}