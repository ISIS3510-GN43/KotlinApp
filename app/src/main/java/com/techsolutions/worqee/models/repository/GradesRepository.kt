package com.techsolutions.worqee.models.repository

import android.content.Context
import com.techsolutions.worqee.models.analytics.GradeUsageTracker
import com.techsolutions.worqee.models.clases.Materia
import com.techsolutions.worqee.models.clases.Nota
import com.techsolutions.worqee.models.storage.PendingAction
import com.techsolutions.worqee.models.storage.PendingSyncManager
import com.techsolutions.worqee.utils.ConnectivityHelper

object GradesRepository {

    fun getMateriasActivas(): List<Materia> {
        return ScheduleRepository.getMateriasActivas().map {
            it.copy(notas = it.notas.toMutableList())
        }
    }

    fun findMateriaByIdOrName(
        materiaId: String?,
        materiaNombre: String?
    ): Materia? {
        val horario = ScheduleRepository.getHorarioActivo() ?: return null

        return horario.materias.find { it.id == materiaId }
            ?: horario.materias.find { it.nombre == materiaNombre }
    }

    fun agregarActividad(
        context: Context,
        materia: Materia,
        nombre: String,
        nota: Float,
        porcentaje: Float
    ): GradeOperationResult {
        val nuevaNota = Nota(
            grade = nota.toDouble(),
            porcentaje = porcentaje.toDouble(),
            titulo = nombre
        )

        materia.notas.add(nuevaNota)
        guardarUsuarioEnCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeUsageTracker.trackGradeAdded(
                materiaId = materia.id,
                materiaNombre = materia.nombre,
                notaTitulo = nombre
            )

            GradeOperationResult.Success(
                isOffline = false,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        } else {
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type = "add",
                    materiaId = materia.id,
                    materiaNombre = materia.nombre,
                    notaTitulo = nombre,
                    notaGrade = nota.toDouble(),
                    notaPorcentaje = porcentaje.toDouble()
                )
            )

            GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = true
            )
        }
    }

    fun eliminarActividad(
        context: Context,
        materia: Materia,
        nota: Nota
    ): GradeOperationResult {
        materia.notas.remove(nota)
        guardarUsuarioEnCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeOperationResult.Success(
                isOffline = false,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        } else {
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type = "delete",
                    materiaId = materia.id,
                    materiaNombre = materia.nombre,
                    notaTitulo = nota.titulo ?: "",
                    notaGrade = nota.grade,
                    notaPorcentaje = nota.porcentaje
                )
            )

            GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = true
            )
        }
    }

    fun actualizarObjetivo(
        context: Context,
        materia: Materia,
        objetivo: Double
    ): GradeOperationResult {
        materia.objetivo = objetivo
        guardarUsuarioEnCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeUsageTracker.trackObjectiveUpdated(
                materiaId = materia.id,
                materiaNombre = materia.nombre,
                nuevoObjetivo = objetivo
            )

            GradeOperationResult.Success(
                isOffline = false,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        } else {
            GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        }
    }

    fun syncPendingActions(context: Context): GradeOperationResult {
        if (!ConnectivityHelper.isOnline(context)) {
            return GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        }

        val pendingActions = PendingSyncManager.getPendingActions()

        pendingActions.forEach { action ->
            when (action.type) {
                "add" -> {
                    GradeUsageTracker.trackGradeAdded(
                        materiaId = action.materiaId,
                        materiaNombre = action.materiaNombre,
                        notaTitulo = action.notaTitulo
                    )
                }

                "delete" -> {
                    /*
                     * Si luego tienen endpoint real para borrar notas,
                     * aquí se llama. Por ahora solo limpiamos la acción pendiente.
                     */
                }
            }
        }

        PendingSyncManager.clearPendingActions()

        return GradeOperationResult.Success(
            isOffline = false,
            hasPendingSync = false
        )
    }

    fun calcularNotaNecesaria(materia: Materia): Float {
        if (materia.objetivo <= 0.0) return -1f
        if (materia.notas.isEmpty()) return -2f

        val notaActual = materia.notas.sumOf {
            it.grade * (it.porcentaje / 100.0)
        }.toFloat()

        val porcentajeUsado = materia.notas.sumOf {
            it.porcentaje
        }.toFloat()

        val porcentajeRestante = 100f - porcentajeUsado

        return if (porcentajeRestante <= 0f) {
            if (notaActual >= materia.objetivo.toFloat()) {
                0f
            } else {
                notaActual
            }
        } else {
            val notaFaltante =
                (materia.objetivo.toFloat() - notaActual) / (porcentajeRestante / 100f)

            notaFaltante.coerceIn(0f, 5f)
        }
    }

    fun isOffline(context: Context): Boolean {
        return !ConnectivityHelper.isOnline(context)
    }

    fun hasPendingSync(): Boolean {
        return PendingSyncManager.hasPendingActions()
    }

    private fun guardarUsuarioEnCache() {
        val usuario = SessionRepository.getCurrentUser()

        if (usuario != null) {
            UsuarioRepository.guardarEnCaché(usuario)
        }
    }
}

sealed class GradeOperationResult {
    data class Success(
        val isOffline: Boolean,
        val hasPendingSync: Boolean
    ) : GradeOperationResult()

    data class Error(
        val message: String
    ) : GradeOperationResult()
}