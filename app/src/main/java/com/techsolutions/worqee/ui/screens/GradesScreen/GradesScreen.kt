package com.techsolutions.worqee.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen() {
    val context = LocalContext.current  
    //refrescar la pantalla al volver 
    var refreshTrigger by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        refreshTrigger++
    }
    val usuario = remember(refreshTrigger) { Usuario.getInstance() }
    val horarioActivo = usuario.horarios.firstOrNull { it.activo }
        ?: usuario.horarios.firstOrNull()
    val materias = horarioActivo?.materias ?: emptyList()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grades") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? android.app.Activity)?.finish()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(materias) { materia ->
                key(materia.id, materia.notas.size, materia.objetivo) {
                    MateriaProgressRow(
                        materia = materia,
                        onClick = {
                            val intent = Intent(context as Context, SubjectGradesActivity::class.java)
                            intent.putExtra("materiaNombre", materia.nombre)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MateriaProgressRow(
    materia: Materia,
    onClick: () -> Unit
) {
    val themeBlue = MaterialTheme.colorScheme.primary
    val promedio = if (materia.notas.isEmpty()) 0f
    else {
        var suma = 0f
        materia.notas.forEach { 
            suma += it.grade.toFloat() * (it.porcentaje.toFloat() / 100f)
        }
        suma
    }
    val progress = if (materia.objetivo > 0) {
        (promedio / materia.objetivo.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    modifier = Modifier.weight(1f)
                )

                val percentInt = (progress * 100).toInt()

                Text(
                    text = "${percentInt}%",
                    style = MaterialTheme.typography.bodyMedium,
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
        }
    }
}
