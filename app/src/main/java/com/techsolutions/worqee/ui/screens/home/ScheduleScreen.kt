package com.techsolutions.worqee.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techsolutions.worqee.models.Dia
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.ui.screens.GradesScreen.GradesActivity
import com.techsolutions.worqee.ui.screens.friends.FriendsActivity
import com.techsolutions.worqee.ui.theme.AntiFlashWhite
import com.techsolutions.worqee.ui.theme.BackgroundLight
import com.techsolutions.worqee.ui.theme.BorderLight
import com.techsolutions.worqee.ui.theme.CaribbeanCurrent
import com.techsolutions.worqee.ui.theme.LinkBlue
import com.techsolutions.worqee.ui.theme.Night
import com.techsolutions.worqee.ui.theme.TextSecondary
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techsolutions.worqee.ui.components.NavBarItem
import com.techsolutions.worqee.ui.components.BottomNavBar
import android.content.Context
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.storage.LocalStorageManager
import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(


    onLogout: () -> Unit = {},
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var mostrarMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { mostrarMenu = true },
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    mostrarMenu = false
                                    FirebaseAuth.getInstance().signOut()
                                    Usuario.clearInstance()
                                    LocalStorageManager.limpiarCaché()
                                    val prefs = context.getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
                                    prefs.edit(commit = true) {
                                        remove("userId")
                                    }
                                    onLogout()
                                }
                            )
                        }
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            val context = LocalContext.current
            BottomNavBar(
                selectedItem = NavBarItem.SCHEDULE, // Cambiar según screen

                onItemSelected = { item ->
                    when (item) {
                        NavBarItem.GRADES -> {
                            val intent = Intent(context, GradesActivity::class.java)
                            context.startActivity(intent)
                        }
                        NavBarItem.SCHEDULE -> { }
                        NavBarItem.FRIENDS -> {
                            val intent = Intent(context, FriendsActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Night
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DaysRow(
                days = uiState.availableDays,
                selectedDay = uiState.selectedDay,
                onDaySelected = viewModel::onDaySelected
            )

            ScheduleHeaderActions(
                viewMode = uiState.viewMode,
                onToggleView = viewModel::toggleViewMode
            )

            when (uiState.viewMode) {
                ScheduleViewMode.DAY -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            ScheduleTimeline(materias = uiState.filteredMaterias)
                        }
                    }
                }

                ScheduleViewMode.WEEK -> {
                    WeeklyScheduleGrid(
                        days = uiState.availableDays,
                        materias = uiState.allMaterias,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DaysRow(
    days: List<Dia>,
    selectedDay: Dia?,
    onDaySelected: (Dia) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        days.forEach { day ->
            DayChip(
                day = dayLabel(day),
                selected = selectedDay == day,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@Composable
fun DayChip(
    day: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val dayColor = if (selected) AntiFlashWhite else TextSecondary

    Column(
        modifier = Modifier
            .width(64.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = BorderLight,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelMedium,
            color = dayColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
fun ScheduleHeaderActions(
    viewMode: ScheduleViewMode,
    onToggleView: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(onClick = onToggleView) {
            Text(
                text = if (viewMode == ScheduleViewMode.DAY) {
                    "Vista semanal"
                } else {
                    "Vista diaria"
                }
            )
        }
    }
}
@Composable
fun ScheduleTimeline(materias: List<Materia>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (materias.isEmpty()) {
            Text(
                text = "No hay materias para este día",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            return@Column
        }

        materias.forEachIndexed { index, materia ->
            val horaLabel = formatHourLabel(materia.horaInicio.firstOrNull())
            TimeSlot(hora = horaLabel, materia = materia)
        }
    }
}

@Composable
fun TimeSlot(hora: String, materia: Materia) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = hora,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                modifier = Modifier
                    .height(96.dp)
                    .width(1.dp)
                    .padding(start = 16.dp),
                color = BorderLight
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        ClassCard(materia)
    }
}

@Composable
fun ClassCard(materia: Materia) {
    val parsedColor = parseHexColor(materia.color)
    val tagColor = parsedColor.copy(alpha = 0.18f)
    val tagTextColor = parsedColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = materia.nombre,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .background(tagColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = materia.profesor.ifBlank { "MATERIA" },
                        style = MaterialTheme.typography.labelSmall,
                        color = tagTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatHourRange(
                    materia.horaInicio.firstOrNull(),
                    materia.horaFin.firstOrNull()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Ubicación",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = materia.aula.joinToString(" · ").ifBlank { "Sin aula" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}



@Composable
fun WeeklyScheduleGrid(
    days: List<Dia>,
    materias: List<Materia>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        days.forEach { day ->
            val dayMaterias = materias
                .filter { it.dias.contains(day) }
                .sortedBy { it.horaInicio.firstOrNull() ?: Int.MAX_VALUE }

            WeeklyDayColumn(day = day, materias = dayMaterias)
        }
    }
}

@Composable
fun WeeklyDayColumn(
    day: Dia,
    materias: List<Materia>
) {
    Column(
        modifier = Modifier.width(140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(14.dp)
                )
                .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayLabel(day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (materias.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin clases",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else {
            materias.forEach { materia ->
                WeeklyClassCard(materia)
            }
        }
    }
}

@Composable
fun WeeklyClassCard(materia: Materia) {
    val parsedColor = parseHexColor(materia.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = parsedColor.copy(alpha = 0.14f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = materia.nombre,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatHourRange(
                    materia.horaInicio.firstOrNull(),
                    materia.horaFin.firstOrNull()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = materia.aula.joinToString(" · ").ifBlank { "Sin aula" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

fun dayLabel(dia: Dia): String = dia.toString()

fun formatHourLabel(hour: Int?): String {
    if (hour == null) return "--:--"
    val h = hour / 100
    val m = hour % 100
    return String.format("%02d:%02d", h, m)
}

fun formatHourRange(start: Int?, end: Int?): String {
    if (start == null || end == null) return "--:--"
    return "${formatHourLabel(start)} - ${formatHourLabel(end)}"
}

fun parseHexColor(hex: String): Color {
    return try {
        val normalized = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(normalized))
    } catch (_: Exception) {
        LinkBlue
    }
}