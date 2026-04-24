package com.techsolutions.worqee.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.perf.FirebasePerformance
import com.techsolutions.worqee.models.clases.Materia
import com.techsolutions.worqee.models.clases.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesViewModel : ViewModel() {

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materiasState: StateFlow<List<Materia>> = _materias.asStateFlow()

    // Key: materia.id (no nombre, para evitar colisiones con materias duplicadas)
    // Valores especiales: -1f = sin objetivo, -2f = sin actividades
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
        _notasNecesarias.value = emptyMap()
        loadMaterias()
    }

    fun calcularNotaNecesaria(materia: Materia): Float {
        // Caso 1: sin objetivo definido
        if (materia.objetivo <= 0.0) {
            _notasNecesarias.value = _notasNecesarias.value + (materia.id to -1f)
            return -1f
        }

        // Caso 2: sin actividades — no hay notas que respalden un "ya pasaste"
        if (materia.notas.isEmpty()) {
            _notasNecesarias.value = _notasNecesarias.value + (materia.id to -2f)
            return -2f
        }

        // Firebase Performance trace - Telemetria
        val trace = FirebasePerformance.getInstance().newTrace("calcular_nota_necesaria")
        trace.start()

        val inicio = System.currentTimeMillis()

        val notaActual = materia.notas.sumOf { it.grade * (it.porcentaje / 100.0) }.toFloat()
        val porcentajeUsado = materia.notas.sumOf { it.porcentaje }.toFloat()
        val porcentajeRestante = 100f - porcentajeUsado

        val resultado = if (porcentajeRestante <= 0f) {
            // Ya se calificó todo — comparar promedio vs objetivo
            if (notaActual >= materia.objetivo.toFloat()) 0f else notaActual
        } else {
            val notaFaltante = (materia.objetivo.toFloat() - notaActual) / (porcentajeRestante / 100f)
            notaFaltante.coerceIn(0f, 5f)
        }

        val fin = System.currentTimeMillis()
        val tiempoTranscurrido = fin - inicio
        tiemposCalculo.add(tiempoTranscurrido)

        trace.putAttribute("materia", materia.nombre.take(100))
        trace.putMetric("tiempo_ms", tiempoTranscurrido)
        trace.putMetric("supera_100ms", if (tiempoTranscurrido > 100L) 1L else 0L)
        trace.stop()

        // Key por id, no por nombre, razon de por allá arriba
        _notasNecesarias.value = _notasNecesarias.value + (materia.id to resultado)

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