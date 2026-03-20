package com.techsolutions.worqee

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.storage.LocalStorageManager
import com.techsolutions.worqee.ui.screens.home.ScheduleScreen
import com.techsolutions.worqee.ui.screens.login.LoginScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el LocalStorageManager (lo tenían tus compañeros)
        LocalStorageManager.init(applicationContext)

        // Revisamos si hay un userId guardado en caché
        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", "7rivfdd9FBTrlSVLT6sJcb3yZLK2")

        lifecycleScope.launch {

            // Si hay userId guardado, cargamos el usuario
            val estaLogueado = if (userId != null) {
                // Primero intentamos desde el caché local
                val usuarioCache = UsuarioRepository.cargarDelCaché()
                if (usuarioCache != null) {
                    Log.d("MainActivity", "Usuario cargado desde caché")
                    true
                } else {
                    // Si no hay caché, lo buscamos en el servidor
                    Log.d("MainActivity", "Usuario cargado del servidor")
                    UsuarioRepository.cargarSingletonUsuario(userId)
                }
            } else {
                // No hay userId — el usuario no ha hecho login
                false
            }

            setContent {
                WorqeeTheme {
                    var logueado by remember { mutableStateOf(estaLogueado) }

                    if (logueado) {
                        ScheduleScreen(
                            onLogout = { logueado = false }
                        )
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