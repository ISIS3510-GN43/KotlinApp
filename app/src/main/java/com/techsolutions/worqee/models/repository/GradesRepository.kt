package com.techsolutions.worqee.models.repository

import android.content.Context
import com.techsolutions.worqee.models.analytics.GradeUsageTracker
import com.techsolutions.worqee.models.clases.Grade
import com.techsolutions.worqee.models.clases.Subject
import com.techsolutions.worqee.models.storage.PendingAction
import com.techsolutions.worqee.models.storage.PendingSyncManager
import com.techsolutions.worqee.utils.ConnectivityHelper

object GradesRepository {

    fun getActiveSubjects(): List<Subject> {
        return ScheduleRepository.getActiveSubjects().map {
            it.copy(grades = it.grades.toMutableList())
        }
    }

    fun findSubjectByIdOrName(
        subjectId: String?,
        subjectName: String?
    ): Subject? {
        val schedule = ScheduleRepository.getActiveSchedule() ?: return null

        return schedule.subjects.find { it.id == subjectId }
            ?: schedule.subjects.find { it.name == subjectName }
    }

    fun addGrade(
        context: Context,
        subject: Subject,
        title: String,
        value: Float,
        percentage: Float
    ): GradeOperationResult {
        val newGrade = Grade(
            value = value.toDouble(),
            percentage = percentage.toDouble(),
            title = title
        )

        subject.grades.add(newGrade)
        saveUserInCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeUsageTracker.trackGradeAdded(
                subjectId = subject.id,
                subjectName = subject.name,
                gradeTitle = title
            )

            GradeOperationResult.Success(
                isOffline = false,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        } else {
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type = "add",
                    subjectId = subject.id,
                    subjectName = subject.name,
                    gradeTitle = title,
                    gradeValue = value.toDouble(),
                    gradePercentage = percentage.toDouble()
                )
            )

            GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = true
            )
        }
    }

    fun deleteGrade(
        context: Context,
        subject: Subject,
        grade: Grade
    ): GradeOperationResult {
        subject.grades.remove(grade)
        saveUserInCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeOperationResult.Success(
                isOffline = false,
                hasPendingSync = PendingSyncManager.hasPendingActions()
            )
        } else {
            PendingSyncManager.addPendingAction(
                PendingAction(
                    type = "delete",
                    subjectId = subject.id,
                    subjectName = subject.name,
                    gradeTitle = grade.title ?: "",
                    gradeValue = grade.value,
                    gradePercentage = grade.percentage
                )
            )

            GradeOperationResult.Success(
                isOffline = true,
                hasPendingSync = true
            )
        }
    }

    fun updateObjective(
        context: Context,
        subject: Subject,
        objective: Double
    ): GradeOperationResult {
        subject.objective = objective
        saveUserInCache()

        return if (ConnectivityHelper.isOnline(context)) {
            GradeUsageTracker.trackObjectiveUpdated(
                subjectId = subject.id,
                subjectName = subject.name,
                newObjective = objective
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
                        subjectId = action.subjectId,
                        subjectName = action.subjectName,
                        gradeTitle = action.gradeTitle
                    )
                }

                "delete" -> {
                    /*
                     * If a real endpoint for deleting grades is added later,
                     * call it here. For now, only clear the pending action.
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

    fun calculateRequiredGrade(subject: Subject): Float {
        if (subject.objective <= 0.0) return -1f
        if (subject.grades.isEmpty()) return -2f

        val currentGrade = subject.grades.sumOf {
            it.value * (it.percentage / 100.0)
        }.toFloat()

        val usedPercentage = subject.grades.sumOf {
            it.percentage
        }.toFloat()

        val remainingPercentage = 100f - usedPercentage

        return if (remainingPercentage <= 0f) {
            if (currentGrade >= subject.objective.toFloat()) {
                0f
            } else {
                currentGrade
            }
        } else {
            val missingGrade =
                (subject.objective.toFloat() - currentGrade) / (remainingPercentage / 100f)

            missingGrade.coerceIn(0f, 5f)
        }
    }

    fun isOffline(context: Context): Boolean {
        return !ConnectivityHelper.isOnline(context)
    }

    fun hasPendingSync(): Boolean {
        return PendingSyncManager.hasPendingActions()
    }

    private fun saveUserInCache() {
        val user = SessionRepository.getCurrentUser()

        if (user != null) {
            UserRepository.saveToCache(user)
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