package com.techsolutions.worqee.ui.screens.GradesScreen.subjectGrades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import com.techsolutions.worqee.viewModel.SubjectGradesViewModel
import com.techsolutions.worqee.viewModel.SubjectGradesViewModelFactory

class SubjectGradesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val materiaId = intent.getStringExtra("materiaId")
        val materiaNombre = intent.getStringExtra("materiaNombre")

        val usuario = Usuario.getInstance()
        val horario = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()


        val materia = horario?.materias?.find { it.id == materiaId }
            ?: horario?.materias?.find { it.nombre == materiaNombre }

        if (materia != null) {
            val factory = SubjectGradesViewModelFactory(materia)
            val viewModel: SubjectGradesViewModel by viewModels { factory }
            setContent {
                WorqeeTheme {
                    SubjectGradesScreen(viewModel)
                }
            }
        }
    }
}