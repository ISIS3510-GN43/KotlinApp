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
import com.techsolutions.worqee.models.clases.Grade
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

    val subject by viewModel.subjectState.collectAsState()
    val isAtRisk by viewModel.isAtRisk.collectAsState()
    val addedPercentage by viewModel.addedPercentage.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val hasPendingSync by viewModel.hasPendingSync.collectAsState()

    var activityName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }
    var objective by remember {
        mutableStateOf(if (subject.objective > 0) subject.objective.toString() else "")
    }

    val average = subject.calculateAverage()
    val progressValue = subject.calculateProgress()
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
                        text = subject.name,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
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
                                text = "Sin conexión",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCC0000)
                            )

                            if (hasPendingSync) {
                                Text(
                                    text = "Cambios pendientes de sincronizar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (hasPendingSync) {
                            IconButton(
                                onClick = {
                                    viewModel.syncPendingActions()
                                }
                            ) {
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

            if (isAtRisk) {
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
                                text = "⚠ Estás en riesgo",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )

                            Text(
                                text = "Has completado el ${addedPercentage.toInt()}% de calificaciones, pero tu promedio es ${"%.2f".format(average)}. Necesitas mejorar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Text(
                text = "Objetivo",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = objective,
                onValueChange = { input ->
                    val number = input.toFloatOrNull()

                    if (input.isEmpty() || (number != null && number in 0f..5f)) {
                        objective = input
                        viewModel.updateObjective(input)
                    }
                },
                label = { Text("Nota objetivo") },
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

            Text(
                text = "Promedio actual: %.2f".format(average),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Agregar actividad",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            OutlinedTextField(
                value = activityName,
                onValueChange = { activityName = it },
                label = { Text("Nombre de la actividad") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = grade,
                    onValueChange = { input ->
                        val number = input.toFloatOrNull()

                        if (input.isEmpty() || (number != null && number in 0f..5f)) {
                            grade = input
                        }
                    },
                    label = { Text("Nota") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = percentage,
                    onValueChange = { input ->
                        val number = input.toFloatOrNull()

                        if (input.isEmpty() || (number != null && number in 0.1f..100f)) {
                            percentage = input
                        }
                    },
                    label = { Text("Porcentaje %") },
                    modifier = Modifier.weight(1f),
                    colors = textFieldColors
                )
            }

            Button(
                onClick = {
                    val gradeValue = grade.toFloatOrNull()
                    val percentageValue = percentage.toFloatOrNull()

                    if (
                        gradeValue != null &&
                        percentageValue != null &&
                        activityName.isNotBlank()
                    ) {
                        viewModel.addGrade(activityName, gradeValue, percentageValue)

                        activityName = ""
                        grade = ""
                        percentage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeBlue,
                    contentColor = TextPrimary
                )
            ) {
                Text("Agregar actividad")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Actividades",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = subject.grades,
                    key = { "${it.title}-${it.percentage}-${it.value}" }
                ) { activity: Grade ->
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
                                    text = activity.title ?: "Actividad",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "Porcentaje: ${activity.percentage}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = "${activity.value}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )

                            IconButton(
                                onClick = {
                                    viewModel.deleteGrade(activity)
                                }
                            ) {
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