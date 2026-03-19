package com.techsolutions.worqee

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.storage.LocalStorageManager
import com.techsolutions.worqee.ui.screens.home.ScheduleScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar el LocalStorageManager antes de cualquier operación
        LocalStorageManager.init(applicationContext)
        
        lifecycleScope.launch {
            if (!Usuario.isInitialized()) {
                // Primero intentar cargar del caché usando el Repository
                val usuarioCaché = UsuarioRepository.cargarDelCaché()
                if (usuarioCaché != null) {
                    Usuario.setInstance(usuarioCaché)
                    Log.d("MainActivity", "Usuario cargado desde caché")
                } else {
                    // Si no hay en caché, cargar del servidor
                    val userId = "7rivfdd9FBTrlSVLT6sJcb3yZLK2"
                    UsuarioRepository.cargarSingletonUsuario(userId)
                    Log.d("MainActivity", "Usuario cargado del servidor")
                }
            }
            setContent {
                WorqeeTheme {
                    ScheduleScreen()
                }
            }
        }
    }
}