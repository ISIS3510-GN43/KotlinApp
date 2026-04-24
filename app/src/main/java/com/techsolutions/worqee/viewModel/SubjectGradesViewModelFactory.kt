package com.techsolutions.worqee.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techsolutions.worqee.models.clases.Materia

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