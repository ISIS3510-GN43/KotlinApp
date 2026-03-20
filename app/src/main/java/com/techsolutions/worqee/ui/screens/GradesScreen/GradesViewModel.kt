package com.techsolutions.worqee.ui.screens.GradesScreen

import androidx.lifecycle.ViewModel
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesViewModel : ViewModel() {
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materiasState: StateFlow<List<Materia>> = _materias.asStateFlow()
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
        loadMaterias()
    }
    fun calcularNotaNecesaria(materia: Materia): Float {


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

