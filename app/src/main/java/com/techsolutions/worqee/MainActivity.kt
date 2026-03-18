package com.techsolutions.worqee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.ui.screens.home.ScheduleScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (!Usuario.isInitialized()) {
                // TODO: reemplazar con el ID real del usuario logueado
                val userId = "7rivfdd9FBTrlSVLT6sJcb3yZLK2"
                UsuarioRepository.cargarSingletonUsuario(userId)
            }

            setContent {
                WorqeeTheme {
                    ScheduleScreen()
                }
            }
        }
    }
}