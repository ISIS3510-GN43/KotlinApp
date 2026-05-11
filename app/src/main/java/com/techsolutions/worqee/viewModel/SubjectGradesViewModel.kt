package com.techsolutions.worqee.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.clases.Grade
import com.techsolutions.worqee.models.clases.Subject
import com.techsolutions.worqee.models.repository.GradeOperationResult
import com.techsolutions.worqee.models.repository.GradesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectGradesViewModel(
    private val subject: Subject,
    private val context: Context
) : ViewModel() {

    private val _subjectState =
        MutableStateFlow(subject.copy(grades = subject.grades.toMutableList()))
    val subjectState: StateFlow<Subject> = _subjectState.asStateFlow()

    private val _isAtRisk = MutableStateFlow(subject.isAtRisk())
    val isAtRisk: StateFlow<Boolean> = _isAtRisk.asStateFlow()

    private val _addedPercentage =
        MutableStateFlow(subject.getAddedPercentage())
    val addedPercentage: StateFlow<Double> = _addedPercentage.asStateFlow()

    private val _isOffline =
        MutableStateFlow(GradesRepository.isOffline(context))
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _hasPendingSync =
        MutableStateFlow(GradesRepository.hasPendingSync())
    val hasPendingSync: StateFlow<Boolean> = _hasPendingSync.asStateFlow()

    init {
        syncPendingActions()
    }

    fun getSubject(): Subject {
        return subject
    }

    fun addGrade(
        title: String,
        value: Float,
        percentage: Float
    ) {
        val result = GradesRepository.addGrade(
            context = context,
            subject = subject,
            title = title,
            value = value,
            percentage = percentage
        )

        applyResult(result)
        updateSubjectState()
    }

    fun deleteGrade(grade: Grade) {
        val result = GradesRepository.deleteGrade(
            context = context,
            subject = subject,
            grade = grade
        )

        applyResult(result)
        updateSubjectState()
    }

    fun updateObjective(objective: String) {
        val objectiveValue = objective.toDoubleOrNull() ?: 0.0

        val result = GradesRepository.updateObjective(
            context = context,
            subject = subject,
            objective = objectiveValue
        )

        applyResult(result)
        updateSubjectState()
    }

    fun syncPendingActions() {
        viewModelScope.launch {
            val result = GradesRepository.syncPendingActions(context)
            applyResult(result)
        }
    }

    private fun applyResult(result: GradeOperationResult) {
        when (result) {
            is GradeOperationResult.Success -> {
                _isOffline.value = result.isOffline
                _hasPendingSync.value = result.hasPendingSync
            }

            is GradeOperationResult.Error -> {
                // Add an error state later if messages need to be shown in the UI.
            }
        }
    }

    private fun updateSubjectState() {
        _subjectState.value = subject.copy(grades = subject.grades.toMutableList())
        _isAtRisk.value = subject.isAtRisk()
        _addedPercentage.value = subject.getAddedPercentage()
    }

    fun calculateAverage(): Float {
        return subject.calculateAverage()
    }

    fun calculateProgress(): Float {
        return subject.calculateProgress()
    }
}