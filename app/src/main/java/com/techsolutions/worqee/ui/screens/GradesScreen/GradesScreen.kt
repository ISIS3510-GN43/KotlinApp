package com.techsolutions.worqee.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techsolutions.worqee.models.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen() {
    val usuario = Usuario.getInstance()
    val horarioActivo = usuario.horarios.firstOrNull { it.activo }
        ?: usuario.horarios.firstOrNull()
    val materias = horarioActivo?.materias ?: emptyList()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grades") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(materias) { materia ->
                Text(
                    text = materia.nombre,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}