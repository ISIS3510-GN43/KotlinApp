package com.techsolutions.worqee.ui.screens.friends

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import com.techsolutions.worqee.storage.LocalStorageManager
import com.techsolutions.worqee.ui.theme.WorqeeTheme

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Patron Repository - sin esto no se puede leer el cache
        LocalStorageManager.init(applicationContext)

        setContent {
            WorqeeTheme {
                var listo by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    listo = try {

                        Usuario.getInstance()
                        true
                    } catch (e: IllegalStateException) {

                        val prefs = getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
                        val userId = prefs.getString("userId", null)
                        if (userId != null) {
                            val fromCache = UsuarioRepository.cargarDelCaché()
                            if (fromCache != null) true
                            else UsuarioRepository.cargarSingletonUsuario(userId)
                        } else {
                            false
                        }
                    }
                }

                when (listo) {
                    null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    true -> FriendsScreen()
                    false -> finish()
                }
            }
        }
    }
}