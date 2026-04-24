package com.techsolutions.worqee.ui.screens.GradesScreen.subjectGrades

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techsolutions.worqee.models.Materia

class SubjectGradesViewModelFactory(
    private val materia: Materia,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectGradesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectGradesViewModel(materia, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}