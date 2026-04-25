package com.techsolutions.worqee.views.screens.GradesScreen.subjectGrades

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.analytics.GradeUsageTracker
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Nota
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.storage.PendingAction
import com.techsolutions.worqee.storage.PendingSyncManager
import com.techsolutions.worqee.utils.ConnectivityHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectGradesViewModel(
    private val materia: Materia,
    private val context: Context
) : ViewModel() {

    private val _materiaState = MutableStateFlow(materia.copy(notas = materia.notas.toMutableList()))
    val materiaState: StateFlow<Materia> = _materiaState.asStateFlow()

    private val _estáEnRiesgo = MutableStateFlow(materia.estáEnRiesgo())
    val estáEnRiesgo: StateFlow<Boolean> = _estáEnRiesgo.asStateFlow()

    private val _porcentajeAgregado = MutableStateFlow(materia.obtenerPorcentajeAgregado())
    val porcentajeAgregado: StateFlow<Double> = _porcentajeAgregado.asStateFlow()

    private val _isOffline = MutableStateFlow(!ConnectivityHelper.isOnline(context))
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _hasPendingSync = MutableStateFlow(PendingSyncManager.hasPendingActions())
    val hasPendingSync: StateFlow<Boolean> = _hasPendingSync.asStateFlow()

    init {
        // Al iniciar, intentar sincronizar si hay internet
        if (ConnectivityHelper.isOnline(context) && PendingSyncManager.hasPendingActions()) {
            syncPendingActions()
        }
    }

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

        if (ConnectivityHelper.isOnline(context)) {
            _isOffline.value = false
            GradeUsageTracker.trackGradeAdded(
                materiaId     = materia.id,
                materiaNombre = materia.nombre,
                notaTitulo    = nombre
            )
            Log.d("SubjectGradesViewModel", "Actividad agregada y sincronizada: $nombre")
        } else {
            _isOffline.value = true
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type          = "add",
                    materiaId     = materia.id,
                    materiaNombre = materia.nombre,
                    notaTitulo    = nombre,
                    notaGrade     = nota.toDouble(),
                    notaPorcentaje = porcentaje.toDouble()
                )
            )
            _hasPendingSync.value = true
            Log.d("SubjectGradesViewModel", "Sin internet - actividad guardada localmente: $nombre")
        }
    }

    fun eliminarActividad(nota: Nota) {
        materia.notas.remove(nota)
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
        actualizarEstadoRiesgo()
        guardarEnCaché()

        if (ConnectivityHelper.isOnline(context)) {
            _isOffline.value = false
            Log.d("SubjectGradesViewModel", "Actividad eliminada y sincronizada: ${nota.titulo}")
        } else {
            _isOffline.value = true
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type           = "delete",
                    materiaId      = materia.id,
                    materiaNombre  = materia.nombre,
                    notaTitulo     = nota.titulo ?: "",
                    notaGrade      = nota.grade,
                    notaPorcentaje = nota.porcentaje
                )
            )
            _hasPendingSync.value = true
            Log.d("SubjectGradesViewModel", "Sin internet - eliminación guardada localmente: ${nota.titulo}")
        }
    }

    fun actualizarObjetivo(objetivo: String) {
        val objValue = objetivo.toDoubleOrNull() ?: 0.0
        materia.objetivo = objValue
        _materiaState.value = materia.copy(notas = materia.notas.toMutableList())
        actualizarEstadoRiesgo()
        guardarEnCaché()

        if (ConnectivityHelper.isOnline(context)) {
            _isOffline.value = false
            GradeUsageTracker.trackObjectiveUpdated(
                materiaId     = materia.id,
                materiaNombre = materia.nombre,
                nuevoObjetivo = objValue
            )
        } else {
            _isOffline.value = true
        }

        Log.d("SubjectGradesViewModel", "Objetivo actualizado a: $objValue")
    }

    fun syncPendingActions() {
        viewModelScope.launch {
            if (!ConnectivityHelper.isOnline(context)) return@launch

            val pending = PendingSyncManager.getPendingActions()
            if (pending.isEmpty()) return@launch

            Log.d("SubjectGradesViewModel", "Sincronizando ${pending.size} acciones pendientes...")

            pending.forEach { action ->
                when (action.type) {
                    "add" -> {
                        GradeUsageTracker.trackGradeAdded(
                            materiaId     = action.materiaId,
                            materiaNombre = action.materiaNombre,
                            notaTitulo    = action.notaTitulo
                        )
                    }
                    "delete" -> {
                        Log.d("SubjectGradesViewModel", "Sincronizando eliminación: ${action.notaTitulo}")
                    }
                }
            }

            PendingSyncManager.clearPendingActions()
            _hasPendingSync.value = false
            _isOffline.value = false
            Log.d("SubjectGradesViewModel", "Sincronización completada")
        }
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