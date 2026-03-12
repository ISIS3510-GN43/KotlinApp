package com.techsolutions.worqee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.techsolutions.worqee.ui.screens.ScheduleScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import com.techsolutions.worqee.models.Dia
import com.techsolutions.worqee.models.Horario
import com.techsolutions.worqee.models.Materia
import com.techsolutions.worqee.models.Usuario
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Usuario.isInitialized()) {
            Usuario.setInstance(crearUsuarioMock())
        }

        setContent {
            WorqeeTheme {
                ScheduleScreen()
            }
        }
    }
}

private fun crearUsuarioMock(): Usuario {
    val horario1 = Horario(
        id = "55944283663-8f44-41fe-a308-2526b7157e43",
        titulo = "Horario1",
        primerDia = diaFromString("LUNES"),
        ultimoDia = diaFromString("VIERNES"),
        fondoPantalla = "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fondos%2F0YA0rN6jFThCg0yhjzCFOM0FxLh2%2F1000045025.jpg?alt=media&token=82423399-38fc-49e5-b5bb-95f04643c4e5",
        activo = true,
        materias = mutableListOf(
            Materia(
                id = "1235a7355bd-4d40-44e7-b3f4-0470a8b96dc5",
                nombre = "ARQUITECTURA Y DISEÑO DE SOFTWARE",
                aula = mutableListOf(".SD_702", ".SD_702", ".B_402"),
                dias = mutableListOf(
                    diaFromString("LUNES"),
                    diaFromString("MARTES"),
                    diaFromString("VIERNES")
                ),
                horaInicio = mutableListOf(800, 800, 800),
                horaFin = mutableListOf(920, 920, 920),
                color = "#cddc39",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "530e04c9ff9-9571-4d45-b92c-eecc4c4edbe7",
                nombre = "INFRAESTRUCTURA COMPUTACIONAL",
                aula = mutableListOf(".RGD_07", ".RGD_07", ".ML_510"),
                dias = mutableListOf(
                    diaFromString("LUNES"),
                    diaFromString("JUEVES"),
                    diaFromString("VIERNES")
                ),
                horaInicio = mutableListOf(930, 930, 930),
                horaFin = mutableListOf(1050, 1050, 1050),
                color = "#2196f3",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "54250b9c93c-ecdd-49bc-a995-3310fe854f33",
                nombre = "ARQUITECTURA EMPRESARIAL",
                aula = mutableListOf(".R_113", ".R_113"),
                dias = mutableListOf(
                    diaFromString("MIERCOLES"),
                    diaFromString("JUEVES")
                ),
                horaInicio = mutableListOf(800, 800),
                horaFin = mutableListOf(920, 920),
                color = "",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "937d79a00f7-e033-42ad-bd38-a080976eac92",
                nombre = "PROBABILIDAD Y ESTADÍSTICA I",
                aula = mutableListOf(".SD_716", ".SD_716"),
                dias = mutableListOf(
                    diaFromString("MIÉRCOLES"),
                    diaFromString("VIERNES")
                ),
                horaInicio = mutableListOf(1100, 1100),
                horaFin = mutableListOf(1220, 1220),
                color = "#00bcd4",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "6312fbfc03f-fddb-4bc8-ac5d-981abbbb4fee",
                nombre = "ENGLISH 09A WRITING 2 - FOR SCIENCE AND ENGINEERING (CICLO 1 DE 8 SEMANAS)",
                aula = mutableListOf(".AU_308", ".SD_307", ".AU_401"),
                dias = mutableListOf(
                    diaFromString("LUNES"),
                    diaFromString("MIÉRCOLES"),
                    diaFromString("VIERNES")
                ),
                horaInicio = mutableListOf(1230, 1230, 1230),
                horaFin = mutableListOf(1350, 1350, 1350),
                color = "#795548",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 3, 14, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "7636db1ee9e-8c1c-4c28-ae1a-a7448eb7fa30",
                nombre = "TRABAJO ASISTIDO PROBABILIDAD Y ESTADÍSTICA I",
                aula = mutableListOf(".ML_208"),
                dias = mutableListOf(diaFromString("LUNES")),
                horaInicio = mutableListOf(1400),
                horaFin = mutableListOf(1520),
                color = "#e91e63",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "524d8d05289-de84-4bbe-a7eb-e7fa28c6a15e",
                nombre = "ANÁLISIS DE DECISIÓN DE INVERSIÓN",
                aula = mutableListOf(".RGD_202", ".RGD_202"),
                dias = mutableListOf(
                    diaFromString("MARTES"),
                    diaFromString("JUEVES")
                ),
                horaInicio = mutableListOf(1400, 1400),
                horaFin = mutableListOf(1520, 1520),
                color = "#9e9e9e",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            ),
            Materia(
                id = "5864aae8e2f-3217-488f-a3ef-c7f972cb3ec7",
                nombre = "DISEÑO DE PRODUCTOS E INNOVACIÓN EN TI",
                aula = mutableListOf(".AU_103-4", ".AU_103-4"),
                dias = mutableListOf(
                    diaFromString("MARTES"),
                    diaFromString("JUEVES")
                ),
                horaInicio = mutableListOf(1530, 1530),
                horaFin = mutableListOf(1650, 1650),
                color = "#ff9800",
                fechaInicio = LocalDateTime.of(2025, 1, 20, 19, 0),
                fechaFin = LocalDateTime.of(2025, 5, 23, 19, 0),
                profesor = "MATERIA"
            )
        )
    )

    val horario2 = Horario(
        id = "31014be34f1-462c-42d9-aefe-34cdd98fbfd7",
        titulo = "Horario2",
        primerDia = diaFromString("LUNES"),
        ultimoDia = diaFromString("SÁBADO"),
        fondoPantalla = "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fondos%2F0YA0rN6jFThCg0yhjzCFOM0FxLh2%2F1000045025.jpg?alt=media&token=700c719f-493e-4124-82e3-d8a52ba51b9c",
        activo = false,
        materias = mutableListOf()
    )

    return Usuario(
        id = "0YA0rN6jFThCg0yhjzCFOM0FxLh2",
        gmail = "sebastianmartinezarias@gmail.com",
        username = "PCRNANO",
        password = "",
        cumpleanios = "2005-05-12",
        amigosIds = mutableListOf(),
        amigosUsernames = mutableListOf(),
        horarios = mutableListOf(horario1, horario2),
        solicitudes = mutableListOf(),
        eventos = mutableListOf(),
        foto = "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fotos%20de%20perfil%2F1000044388.jpg?alt=media&token=ba476cd0-6401-4bd6-9927-a2c236a1b171"
    )
}

private fun diaFromString(value: String): Dia {
    return Dia.fromJson(value)
}