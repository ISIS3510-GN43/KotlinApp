package com.techsolutions.worqee

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.ui.screens.home.ScheduleScreen
import com.techsolutions.worqee.ui.screens.login.LoginScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Abrimos las SharedPreferences donde guardamos el userId
        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)

        // Intentamos leer el userId guardado (null si no hay ninguno)
        val userId = prefs.getString("userId", null)

        lifecycleScope.launch {

            // Verificamos si hay un userId guardado en caché
            val estaLogueado = if (userId != null) {

                UsuarioRepository.cargarSingletonUsuario(userId)
            } else {

                false
            }

            setContent {
                WorqeeTheme {
                    // Usamos un estado para saber qué pantalla mostrar

                    var logueado by remember { mutableStateOf(estaLogueado) }

                    if (logueado) {

                        ScheduleScreen()
                    } else {

                        LoginScreen(
                            onLoginSuccess = { logueado = true }
                        )
                    }
                }
            }
        }
    }
}