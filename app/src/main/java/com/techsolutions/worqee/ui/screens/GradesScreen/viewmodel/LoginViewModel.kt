package com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.techsolutions.worqee.models.Usuario
import com.techsolutions.worqee.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel.LoginViewModel
import com.techsolutions.worqee.ui.screens.GradesScreen.viewmodel.LoginUiState
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState


    fun login(gmail: String, password: String, context: Context) {

        if (gmail.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {

                val resultado = firebaseAuth
                    .signInWithEmailAndPassword(gmail, password)
                    .await()

                val userId = resultado.user?.uid

                if (userId != null) {

                    val prefs = context.getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("userId", userId).apply()


                    UsuarioRepository.cargarSingletonUsuario(userId)

                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error("No se pudo obtener el usuario")
                }

            } catch (e: Exception) {
                val mensaje = when {
                    e.message?.contains("password") == true -> "Contraseña incorrecta"
                    e.message?.contains("email") == true -> "Correo no registrado"
                    e.message?.contains("network") == true -> "Sin conexión a internet"
                    else -> "Error al iniciar sesión"
                }
                _uiState.value = LoginUiState.Error(mensaje)
            }
        }
    }


    fun register(
        gmail: String,
        password: String,
        username: String,
        cumpleanios: String,
        context: Context
    ) {
        if (gmail.isBlank() || password.isBlank() || username.isBlank() || cumpleanios.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {

                val fotoDefault = "https://firebasestorage.googleapis.com/v0/b/techsolutions-eb89a.firebasestorage.app/o/Fotos%20de%20perfil%2Fuser_profile_photo.png?alt=media&token=63c45849-4d40-4805-a222-0e130c588bc8"


                val nuevoUsuario = Usuario(
                    gmail = gmail,
                    password = password,
                    username = username,
                    cumpleanios = cumpleanios,
                    foto = fotoDefault
                )


                val result = UsuarioRepository.register(nuevoUsuario)

                if (result.isSuccess) {
                    val usuarioCreado = result.getOrNull()

                    if (usuarioCreado != null) {
                        // Guardamos el userId que devuelve el backend en caché
                        val prefs = context.getSharedPreferences("worqee_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("userId", usuarioCreado.id).apply()

                        _uiState.value = LoginUiState.Success
                    }
                } else {
                    _uiState.value = LoginUiState.Error("Error al registrarse")
                }

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}