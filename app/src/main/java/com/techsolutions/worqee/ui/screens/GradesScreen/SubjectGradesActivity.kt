package com.techsolutions.worqee.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.ui.theme.WorqeeTheme

import com.techsolutions.worqee.viewmodel.SubjectGradesViewModel


class SubjectGradesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val materiaNombre = intent.getStringExtra("materiaNombre")

        val usuario = Usuario.getInstance()

        val horario = usuario.horarios.firstOrNull { it.activo }
            ?: usuario.horarios.firstOrNull()

        val materia = horario?.materias?.find { it.nombre == materiaNombre }

        if (materia != null) {

            val viewModel = SubjectGradesViewModel(materia)

            setContent {
                WorqeeTheme {
                    SubjectGradesScreen(viewModel)
                }
            }
        }
    }
}