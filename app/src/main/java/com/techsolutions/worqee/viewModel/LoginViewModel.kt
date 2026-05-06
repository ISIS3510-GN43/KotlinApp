package com.techsolutions.worqee.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techsolutions.worqee.models.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class Lockout(val segundosRestantes: Int) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private var failedAttempts = 0
    private val LOCKOUT_MAX_ATTEMPTS = 3
    private val LOCKOUT_DURATION_SECONDS = 30

    fun login(gmail: String, password: String, context: Context) {
        if (_uiState.value is LoginUiState.Lockout) return

        if (gmail.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = AuthRepository.login(gmail, password)

            if (result.isSuccess) {
                failedAttempts = 0
                _uiState.value = LoginUiState.Success
            } else {
                registrarFallo(mapLoginError(result.exceptionOrNull()))
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

            val result = AuthRepository.register(
                gmail = gmail,
                password = password,
                username = username,
                cumpleanios = cumpleanios
            )

            _uiState.value =
                if (result.isSuccess) {
                    LoginUiState.Success
                } else {
                    LoginUiState.Error(
                        result.exceptionOrNull()?.message ?: "Error al registrarse"
                    )
                }
        }
    }

    private fun registrarFallo(mensaje: String) {
        failedAttempts++

        if (failedAttempts >= LOCKOUT_MAX_ATTEMPTS) {
            iniciarLockout()
        } else {
            val intentosRestantes = LOCKOUT_MAX_ATTEMPTS - failedAttempts
            _uiState.value =
                LoginUiState.Error("$mensaje. Te quedan $intentosRestantes intento(s).")
        }
    }

    private fun iniciarLockout() {
        viewModelScope.launch {
            for (segundos in LOCKOUT_DURATION_SECONDS downTo 1) {
                _uiState.value = LoginUiState.Lockout(segundos)
                delay(1000L)
            }

            failedAttempts = 0
            _uiState.value = LoginUiState.Idle
        }
    }

    private fun mapLoginError(error: Throwable?): String {
        val message = error?.message.orEmpty()

        return when {
            message.contains("password", ignoreCase = true) -> "Contraseña incorrecta"
            message.contains("email", ignoreCase = true) -> "Correo no registrado"
            message.contains("network", ignoreCase = true) -> "Sin conexión a internet"
            else -> "Error al iniciar sesión"
        }
    }

    fun resetState() {
        if (_uiState.value !is LoginUiState.Lockout) {
            _uiState.value = LoginUiState.Idle
        }
    }
}