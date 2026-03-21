package com.techsolutions.worqee

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
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


        LocalStorageManager.init(applicationContext)



        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("userId", null)

        lifecycleScope.launch {


            val estaLogueado = if (userId != null) {

                val usuarioCache = UsuarioRepository.cargarDelCaché()
                if (usuarioCache != null) {
                    Log.d("MainActivity", "Usuario cargado desde caché")
                    true
                } else {

                    Log.d("MainActivity", "Usuario cargado del servidor")
                    UsuarioRepository.cargarSingletonUsuario(userId)
                }
            } else {

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