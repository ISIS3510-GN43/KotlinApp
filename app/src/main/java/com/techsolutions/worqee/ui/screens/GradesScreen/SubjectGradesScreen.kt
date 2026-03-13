package com.techsolutions.worqee.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.techsolutions.worqee.models.Nota
import com.techsolutions.worqee.viewmodel.SubjectGradesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectGradesScreen(viewModel: SubjectGradesViewModel) {

    val context = LocalContext.current

    val materia by viewModel.materiaState.collectAsState()

    
    var nombreActividad by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var porcentaje by remember { mutableStateOf("") }

    var objetivo by remember {
        mutableStateOf(if (materia.objetivo > 0) materia.objetivo.toString() else "")
    }

    val promedio = materia.calcularPromedio()
    val progressValue = materia.calcularProgreso()

    val themeBlue = MaterialTheme.colorScheme.primary

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = themeBlue,
        unfocusedBorderColor = themeBlue,
        cursorColor = themeBlue,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(materia.nombre) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Goal")

            OutlinedTextField(
                value = objetivo,
                onValueChange = {
                    objetivo = it
                    viewModel.actualizarObjetivo(it) 
                },
                label = { Text("Target grade") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            LinearProgressIndicator(
                progress = progressValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = themeBlue,
                trackColor = themeBlue.copy(alpha = 0.2f)
            )

            Text("Current Average: %.2f".format(promedio))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Add Activity",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombreActividad,
                onValueChange = { nombreActividad = it },
                label = { Text("Activity name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Grade") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = porcentaje,
                    onValueChange = { porcentaje = it },
                    label = { Text("Weight %") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )
            }

            Button(
                onClick = {

                    val gradeValue = nota.toFloatOrNull()
                    val weightValue = porcentaje.toFloatOrNull()

                    if (gradeValue != null && weightValue != null && nombreActividad.isNotBlank()) {

                        viewModel.agregarActividad(nombreActividad, gradeValue, weightValue)
                        nombreActividad = ""
                        nota = ""
                        porcentaje = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Activity")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Activities",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(materia.notas) { actividad: Nota ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier.weight(1f)) {

                                Text(
                                    actividad.titulo ?: "Activity",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    "Weight: ${actividad.porcentaje}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Text(
                                "${actividad.grade}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}