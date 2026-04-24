package com.techsolutions.worqee.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.techsolutions.worqee.MainActivity
import com.techsolutions.worqee.models.clases.Usuario
import com.techsolutions.worqee.models.repository.UsuarioRepository
import com.techsolutions.worqee.ui.screens.friends.FriendsScreen
import com.techsolutions.worqee.ui.theme.WorqeeTheme

class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            WorqeeTheme {
                var listo by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    listo = try {
                        Usuario.getInstance()
                        true
                    } catch (e: IllegalStateException) {
                        val prefs = requireContext()
                            .getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
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
                    false -> {
                        (activity as? MainActivity)?.mostrarLogin()
                    }
                }
            }
        }
    }
}