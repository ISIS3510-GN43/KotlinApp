package com.techsolutions.worqee.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techsolutions.worqee.models.clases.Subject

class SubjectGradesViewModelFactory(
    private val subject: Subject,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectGradesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectGradesViewModel(subject, context) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}