package com.techsolutions.worqee.ui.screens

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.techsolutions.worqee.ui.screens.GradesScreen.GradesActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.techsolutions.worqee.ui.theme.AntiFlashWhite
import com.techsolutions.worqee.ui.theme.BackgroundLight
import com.techsolutions.worqee.ui.theme.BorderLight
import com.techsolutions.worqee.ui.theme.CaribbeanCurrent
import com.techsolutions.worqee.ui.theme.LinkBlue
import com.techsolutions.worqee.ui.theme.Night
import com.techsolutions.worqee.ui.theme.TextSecondary
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val horarioActivo = Usuario.getInstance().horarios.firstOrNull { it.activo } ?: Usuario.getInstance().horarios.firstOrNull()
    val materias = horarioActivo?.materias ?: mutableListOf()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = horarioActivo?.titulo ?: "My Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.padding(start = 16.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavBar()
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
            DaysRow()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScheduleTimeline(materias = materias)
                }
            }
        }
    }
}

@Composable
fun DaysRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        DayChip("MON", "18", selected = false)
        DayChip("TUE", "19", selected = true)
        DayChip("WED", "20", selected = false)
        DayChip("THU", "21", selected = false)
        DayChip("FRI", "22", selected = false)
        DayChip("SAT", "23", selected = false)
    }
}

@Composable
fun DayChip(day: String, number: String, selected: Boolean) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val dayColor = if (selected) AntiFlashWhite else TextSecondary
    val numColor = if (selected) Night else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .width(48.dp)
            .height(72.dp)
            .background(bg, RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = BorderLight,
                shape = RoundedCornerShape(16.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelSmall,
            color = dayColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = number,
            style = MaterialTheme.typography.titleLarge,
            color = numColor
        )
    }
}

@Composable
fun ScheduleTimeline(materias: List<Materia>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        materias.forEachIndexed { index, materia ->
            val horaLabel = formatHourLabel(materia.horaInicio.firstOrNull())
            TimeSlot(hora = horaLabel, materia = materia)

            if (index == 1) {
                LunchBreak()
            }
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
fun LunchBreak() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = BorderLight
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "LUNCH BREAK",
            style = MaterialTheme.typography.labelMedium,
            color = CaribbeanCurrent.copy(alpha = 0.55f),
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.width(12.dp))
        Divider(
            modifier = Modifier.weight(1f),
            color = BorderLight
        )
    }
}

@Composable
fun BottomNavBar() {
    val context = LocalContext.current
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Schedule") },
            label = { Text("Schedule", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Outlined.People, contentDescription = "Friends") },
            label = { Text("Friends", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {val intent = Intent(context, GradesActivity::class.java)
                        context.startActivity(intent)},
            icon = { Icon(Icons.Outlined.School, contentDescription = "Grades") },
            label = { Text("Grades", style = MaterialTheme.typography.labelSmall) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Outlined.AutoStories, contentDescription = "Study") },
            label = { Text("Study", style = MaterialTheme.typography.labelSmall) }
        )
        
    }
}

fun formatHourLabel(hour: Int?): String {
    if (hour == null) return "--:--"
    return String.format("%02d:00", hour)
}

fun formatHourRange(start: Int?, end: Int?): String {
    if (start == null || end == null) return "--:--"
    return "${to12Hour(start)}-${to12Hour(end)}"
}

fun to12Hour(hour: Int): String {
    val suffix = if (hour >= 12) "PM" else "AM"
    val formattedHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%02d:00 %s", formattedHour, suffix)
}

fun parseHexColor(hex: String): Color {
    return try {
        val normalized = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(normalized))
    } catch (_: Exception) {
        LinkBlue
    }
}