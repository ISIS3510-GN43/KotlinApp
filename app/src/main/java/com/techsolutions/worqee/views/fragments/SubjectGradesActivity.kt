package com.techsolutions.worqee.views.fragments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.techsolutions.worqee.models.repository.GradesRepository
import com.techsolutions.worqee.models.storage.PendingSyncManager
import com.techsolutions.worqee.viewModel.SubjectGradesViewModel
import com.techsolutions.worqee.viewModel.SubjectGradesViewModelFactory
import com.techsolutions.worqee.views.screens.SubjectGradesScreen
import com.techsolutions.worqee.views.theme.WorqeeTheme

class SubjectGradesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PendingSyncManager.init(applicationContext)

        val materiaId = intent.getStringExtra("materiaId")
        val materiaNombre = intent.getStringExtra("materiaNombre")

        val materia = GradesRepository.findMateriaByIdOrName(
            materiaId = materiaId,
            materiaNombre = materiaNombre
        )

        if (materia != null) {
            val factory = SubjectGradesViewModelFactory(
                materia = materia,
                context = applicationContext
            )

            val viewModel: SubjectGradesViewModel by viewModels { factory }

            setContent {
                WorqeeTheme {
                    SubjectGradesScreen(viewModel)
                }
            }
        } else {
            finish()
        }
    }
}