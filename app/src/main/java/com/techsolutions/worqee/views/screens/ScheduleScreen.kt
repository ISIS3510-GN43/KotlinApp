package com.techsolutions.worqee.views.screens

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.techsolutions.worqee.models.clases.Day
import com.techsolutions.worqee.models.clases.Subject
import com.techsolutions.worqee.models.clases.User
import com.techsolutions.worqee.models.storage.LocalStorageManager
import com.techsolutions.worqee.viewModel.ScheduleViewModel
import com.techsolutions.worqee.views.components.BottomNavBar
import com.techsolutions.worqee.views.components.NavBarItem
import com.techsolutions.worqee.views.states.ScheduleViewMode
import com.techsolutions.worqee.views.theme.AntiFlashWhite
import com.techsolutions.worqee.views.theme.BackgroundLight
import com.techsolutions.worqee.views.theme.BorderLight
import com.techsolutions.worqee.views.theme.LinkBlue
import com.techsolutions.worqee.views.theme.Night
import com.techsolutions.worqee.views.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateToGrades: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Más opciones",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { showMenu = true },
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    showMenu = false

                                    FirebaseAuth.getInstance().signOut()
                                    User.clearInstance()
                                    LocalStorageManager.clearSession()

                                    val prefs = context.getSharedPreferences(
                                        "worqee_prefs",
                                        Context.MODE_PRIVATE
                                    )

                                    prefs.edit()
                                        .remove("userId")
                                        .apply()

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
            BottomNavBar(
                selectedItem = NavBarItem.SCHEDULE,
                onItemSelected = { item ->
                    when (item) {
                        NavBarItem.GRADES -> onNavigateToGrades()
                        NavBarItem.SCHEDULE -> Unit
                        NavBarItem.FRIENDS -> onNavigateToFriends()
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar"
                )
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
                            ScheduleTimeline(subjects = uiState.filteredSubjects)
                        }
                    }
                }

                ScheduleViewMode.WEEK -> {
                    WeeklyScheduleGrid(
                        days = uiState.availableDays,
                        subjects = uiState.allSubjects,
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
    days: List<Day>,
    selectedDay: Day?,
    onDaySelected: (Day) -> Unit
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
    val backgroundColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    val dayColor =
        if (selected) AntiFlashWhite else TextSecondary

    Column(
        modifier = Modifier
            .width(64.dp)
            .height(72.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
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
fun ScheduleTimeline(subjects: List<Subject>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (subjects.isEmpty()) {
            Text(
                text = "No hay materias para este día",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            return@Column
        }

        subjects.forEach { subject ->
            val timeLabel = formatHourLabel(subject.startHours.firstOrNull())

            TimeSlot(
                time = timeLabel,
                subject = subject
            )
        }
    }
}

@Composable
fun TimeSlot(
    time: String,
    subject: Subject
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = time,
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

        SubjectCard(subject)
    }
}

@Composable
fun SubjectCard(subject: Subject) {
    val parsedColor = parseHexColor(subject.color)
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
                    text = subject.name,
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
                        text = subject.professor.ifBlank { "MATERIA" },
                        style = MaterialTheme.typography.labelSmall,
                        color = tagTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatHourRange(
                    subject.startHours.firstOrNull(),
                    subject.endHours.firstOrNull()
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
                    text = subject.classrooms.joinToString(" · ").ifBlank { "Sin aula" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun WeeklyScheduleGrid(
    days: List<Day>,
    subjects: List<Subject>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        days.forEach { day ->
            val daySubjects = subjects
                .filter { it.days.contains(day) }
                .sortedBy { it.startHours.firstOrNull() ?: Int.MAX_VALUE }

            WeeklyDayColumn(
                day = day,
                subjects = daySubjects
            )
        }
    }
}

@Composable
fun WeeklyDayColumn(
    day: Day,
    subjects: List<Subject>
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

        if (subjects.isEmpty()) {
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
            subjects.forEach { subject ->
                WeeklySubjectCard(subject)
            }
        }
    }
}

@Composable
fun WeeklySubjectCard(subject: Subject) {
    val parsedColor = parseHexColor(subject.color)

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
                text = subject.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatHourRange(
                    subject.startHours.firstOrNull(),
                    subject.endHours.firstOrNull()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subject.classrooms.joinToString(" · ").ifBlank { "Sin aula" },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

fun dayLabel(day: Day): String {
    return day.toSpanishName()
}

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