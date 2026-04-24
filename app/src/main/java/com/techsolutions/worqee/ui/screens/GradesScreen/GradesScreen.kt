package com.techsolutions.worqee.ui.screens.GradesScreen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.techsolutions.worqee.models.clases.Materia
import com.techsolutions.worqee.ui.screens.GradesScreen.subjectGrades.SubjectGradesActivity
import com.techsolutions.worqee.viewModel.GradesViewModel
import com.techsolutions.worqee.ui.theme.BackgroundLight
import com.techsolutions.worqee.ui.theme.PrimaryActionBlue
import com.techsolutions.worqee.ui.theme.SurfaceLight
import com.techsolutions.worqee.ui.theme.TextPrimary
import com.techsolutions.worqee.ui.theme.TextSecondary
import com.techsolutions.worqee.utils.ConnectivityHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(viewModel: GradesViewModel) {

    val context = LocalContext.current
    val materias by viewModel.materiasState.collectAsState()
    val notasNecesarias by viewModel.notasNecesarias.collectAsState()

    // ✅ Detectar conectividad
    val isOffline = remember { !ConnectivityHelper.isOnline(context) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Grades",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? android.app.Activity)?.finish()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isOffline) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            Column {
                                Text(
                                    "Sin conexión",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFCC0000)
                                )
                                Text(
                                    "Mostrando datos guardados localmente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            items(materias) { materia ->
                MateriaProgressRow(
                    materia = materia,
                    notaNecesaria = notasNecesarias[materia.id],
                    onCalcularNota = { viewModel.calcularNotaNecesaria(materia) },
                    onClick = {
                        val intent = Intent(context, SubjectGradesActivity::class.java)
                        intent.putExtra("materiaId", materia.id)
                        intent.putExtra("materiaNombre", materia.nombre)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                BusinessQuestionCard(
                    promedio = viewModel.obtenerPromedioTiempoCalculo(),
                    superaObjetivo = viewModel.superaObjetivo100ms()
                )
            }
        }
    }
}

@Composable
fun MateriaProgressRow(
    materia: Materia,
    notaNecesaria: Float?,
    onCalcularNota: () -> Unit,
    onClick: () -> Unit
) {
    val themeBlue = PrimaryActionBlue
    val progress = materia.calcularProgreso()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = materia.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                val percentInt = (progress * 100).toInt()
                Text(
                    text = "$percentInt%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = themeBlue,
                trackColor = themeBlue.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { onCalcularNota() }) {
                    Text(
                        "How much do I need to pass?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                notaNecesaria?.let { nota ->
                    when {
                        nota == -1f -> Text(
                            text = "Define un objetivo primero",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        nota == -2f -> Text(
                            text = "Sin actividades aún",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        nota <= 0f -> Text(
                            text = "¡Ya pasaste!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                        else -> Text(
                            text = "Necesitas: ${"%.1f".format(nota)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessQuestionCard(promedio: Long, superaObjetivo: Boolean) {

    if (promedio == 0L) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (superaObjetivo) Color.Red.copy(alpha = 0.1f)
            else Color.Green.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "📊 Business Question",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = "Tiempo promedio de cálculo: ${promedio}ms",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = if (superaObjetivo) "⚠️ Supera el objetivo de 100ms"
                else "✅ Dentro del objetivo de 100ms",
                style = MaterialTheme.typography.bodyMedium,
                color = if (superaObjetivo) Color.Red else Color.Green
            )
        }
    }
}