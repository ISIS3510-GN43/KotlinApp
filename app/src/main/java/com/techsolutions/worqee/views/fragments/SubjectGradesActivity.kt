package com.techsolutions.worqee.views.fragments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.storage.PendingSyncManager
import com.techsolutions.worqee.views.screens.GradesScreen.subjectGrades.SubjectGradesViewModel
import com.techsolutions.worqee.views.screens.SubjectGradesScreen
import com.techsolutions.worqee.views.theme.WorqeeTheme

class SubjectGradesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PendingSyncManager.init(applicationContext)

        val materiaId = intent.getStringExtra("materiaId")
        val materiaNombre = intent.getStringExtra("materiaNombre")

        val usuario = Usuario.getInstance()
        val horario = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()

        val materia = horario?.materias?.find { it.id == materiaId }
            ?: horario?.materias?.find { it.nombre == materiaNombre }

        if (materia != null) {
            val factory = SubjectGradesViewModelFactory(materia, applicationContext)
            val viewModel: SubjectGradesViewModel by viewModels { factory }
            setContent {
                WorqeeTheme {
                    SubjectGradesScreen(viewModel)
                }
            }
        }
    }
}