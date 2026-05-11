package com.techsolutions.worqee.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.perf.FirebasePerformance
import com.techsolutions.worqee.models.clases.Subject
import com.techsolutions.worqee.models.clases.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesViewModel : ViewModel() {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjectsState: StateFlow<List<Subject>> = _subjects.asStateFlow()

    // Key: subject.id, not name, to avoid collisions with duplicate subjects.
    // Special values: -1f = no objective, -2f = no grades.
    private val _requiredGrades = MutableStateFlow<Map<String, Float>>(emptyMap())
    val requiredGrades: StateFlow<Map<String, Float>> = _requiredGrades.asStateFlow()

    val calculationTimes = mutableListOf<Long>()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        try {
            val user = User.getInstance()
            val activeSchedule = user.schedules.firstOrNull { it.isActive }
                ?: user.schedules.firstOrNull()

            val subjectsCopy = activeSchedule?.subjects?.map {
                it.copy(grades = it.grades.toMutableList())
            } ?: emptyList()

            _subjects.value = subjectsCopy
        } catch (e: Exception) {
            _subjects.value = emptyList()
        }
    }

    fun refresh() {
        _requiredGrades.value = emptyMap()
        loadSubjects()
    }

    fun calculateRequiredGrade(subject: Subject): Float {
        if (subject.objective <= 0.0) {
            _requiredGrades.value = _requiredGrades.value + (subject.id to -1f)
            return -1f
        }

        if (subject.grades.isEmpty()) {
            _requiredGrades.value = _requiredGrades.value + (subject.id to -2f)
            return -2f
        }

        val trace = FirebasePerformance.getInstance().newTrace("calculate_required_grade")
        trace.start()

        val startTime = System.currentTimeMillis()

        val currentGrade = subject.grades.sumOf {
            it.value * (it.percentage / 100.0)
        }.toFloat()

        val usedPercentage = subject.grades.sumOf {
            it.percentage
        }.toFloat()

        val remainingPercentage = 100f - usedPercentage

        val result = if (remainingPercentage <= 0f) {
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

        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        calculationTimes.add(elapsedTime)

        trace.putAttribute("subject", subject.name.take(100))
        trace.putMetric("time_ms", elapsedTime)
        trace.putMetric("exceeds_100ms", if (elapsedTime > 100L) 1L else 0L)
        trace.stop()

        _requiredGrades.value = _requiredGrades.value + (subject.id to result)

        return result
    }

    fun getAverageCalculationTime(): Long {
        if (calculationTimes.isEmpty()) return 0L
        return calculationTimes.average().toLong()
    }

    fun exceeds100msObjective(): Boolean {
        return getAverageCalculationTime() > 100L
    }
}