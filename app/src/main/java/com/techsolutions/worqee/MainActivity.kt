package com.techsolutions.worqee

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.storage.LocalStorageManager
import com.techsolutions.worqee.ui.screens.home.ScheduleScreen
import com.techsolutions.worqee.ui.screens.login.LoginScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import com.techsolutions.worqee.analytics.GradeUsageTracker


//IMportante: Cargar aqui, para no depender de crear un activity por screen.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GradeUsageTracker.init(this)
        LocalStorageManager.init(applicationContext)

        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        setContent {
            WorqeeTheme {
                var logueado by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    val estaLogueado = if (userId != null) {
                        val usuarioCache = UsuarioRepository.cargarDelCaché()
                        if (usuarioCache != null) {
                            // Fix: setear el singleton con los datos de la caché
                            Usuario.setInstance(usuarioCache)
                            Log.d("MainActivity", "Usuario cargado desde caché")
                            true
                        } else {
                            Log.d("MainActivity", "Cargando usuario del servidor")
                            UsuarioRepository.cargarSingletonUsuario(userId)
                        }
                    } else {
                        false
                    }
                    logueado = estaLogueado
                }

                when (logueado) {
                    null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    true -> ScheduleScreen(onLogout = { logueado = false })
                    false -> LoginScreen(onLoginSuccess = { logueado = true })
                }
            }
        }
    }
}