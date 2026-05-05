package com.techsolutions.worqee.views.screens

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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.techsolutions.worqee.models.clases.Nota
import com.techsolutions.worqee.viewModel.SubjectGradesViewModel
import com.techsolutions.worqee.views.theme.BackgroundLight
import com.techsolutions.worqee.views.theme.PrimaryActionBlue
import com.techsolutions.worqee.views.theme.SurfaceLight
import com.techsolutions.worqee.views.theme.TextPrimary
import com.techsolutions.worqee.views.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectGradesScreen(viewModel: SubjectGradesViewModel) {
    val context = LocalContext.current
    val materia by viewModel.materiaState.collectAsState()
    val enRiesgo by viewModel.estáEnRiesgo.collectAsState()
    val porcentajeAgregado by viewModel.porcentajeAgregado.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val hasPendingSync by viewModel.hasPendingSync.collectAsState()

    var nombreActividad by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var porcentaje by remember { mutableStateOf("") }
    var objetivo by remember {
        mutableStateOf(if (materia.objetivo > 0) materia.objetivo.toString() else "")
    }

    val promedio = materia.calcularPromedio()
    val progressValue = materia.calcularProgreso()
    val themeBlue = PrimaryActionBlue

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = themeBlue,
        unfocusedBorderColor = themeBlue,
        cursorColor = themeBlue,
        focusedLabelColor = TextPrimary,
        unfocusedLabelColor = TextPrimary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        materia.nombre,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isOffline) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEEEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFCC0000)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Sin conexión",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCC0000)
                            )
                            if (hasPendingSync) {
                                Text(
                                    "Cambios pendientes de sincronizar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        if (hasPendingSync) {
                            IconButton(onClick = { viewModel.syncPendingActions() }) {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = "Sincronizar",
                                    tint = PrimaryActionBlue
                                )
                            }
                        }
                    }
                }
            }

            // Banner en riesgo
            if (enRiesgo) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryActionBlue.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Alerta",
                            tint = TextSecondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "⚠ Estás en riesgo",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                            Text(
                                "Has completado el ${porcentajeAgregado.toInt()}% de calificaciones pero tu promedio es ${"%.2f".format(promedio)}. ¡Necesitas mejorar!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Text("Goal", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = objetivo,
                onValueChange = { input ->
                    val num = input.toFloatOrNull()
                    if (input.isEmpty() || (num != null && num in 0f..5f)) {
                        objetivo = input
                        viewModel.actualizarObjetivo(input)
                    }
                },
                label = { Text("Target grade") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = themeBlue,
                trackColor = themeBlue.copy(alpha = 0.2f)
            )
            Text("Current Average: %.2f".format(promedio), color = TextPrimary)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Add Activity",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
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
                    onValueChange = { input ->
                        val num = input.toFloatOrNull()
                        if (input.isEmpty() || (num != null && num in 0f..5f)) {
                            nota = input
                        }
                    },
                    label = { Text("Grade") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = porcentaje,
                    onValueChange = { input ->
                        val num = input.toFloatOrNull()
                        if (input.isEmpty() || (num != null && num in 0.1f..100f)) {
                            porcentaje = input
                        }
                    },
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
                    contentColor = TextPrimary
                )
            ) {
                Text("Add Activity")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Activities",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    materia.notas,
                    key = { it.titulo + it.porcentaje + it.grade }
                ) { actividad: Nota ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "Weight: ${actividad.porcentaje}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                "${actividad.grade}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            IconButton(onClick = { viewModel.eliminarActividad(actividad) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Eliminar actividad",
                                    tint = PrimaryActionBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}