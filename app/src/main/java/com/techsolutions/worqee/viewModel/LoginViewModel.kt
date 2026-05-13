package com.techsolutions.worqee.viewModel

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
    data class Lockout(val remainingSeconds: Int) : LoginUiState()
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private var failedAttempts = 0

    private val lockoutMaxAttempts = 3
    private val lockoutDurationSeconds = 30

    fun login(email: String, password: String) {
        if (_uiState.value is LoginUiState.Lockout) return

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Completa el correo y la contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = AuthRepository.login(email, password)

            if (result.isSuccess) {
                failedAttempts = 0
                _uiState.value = LoginUiState.Success
            } else {
                registerFailedAttempt(
                    result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register(
        email: String,
        password: String,
        username: String,
        birthday: String
    ) {
        if (email.isBlank() || password.isBlank() || username.isBlank() || birthday.isBlank()) {
            _uiState.value = LoginUiState.Error("Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = AuthRepository.register(
                email = email,
                password = password,
                username = username,
                birthday = birthday
            )

            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success
            } else {
                LoginUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }

    fun resetState() {
        if (_uiState.value !is LoginUiState.Lockout) {
            _uiState.value = LoginUiState.Idle
        }
    }

    private fun registerFailedAttempt(message: String) {
        failedAttempts++

        if (failedAttempts >= lockoutMaxAttempts) {
            startLockout()
        } else {
            val remainingAttempts = lockoutMaxAttempts - failedAttempts

            _uiState.value = LoginUiState.Error(
                "$message. Te quedan $remainingAttempts intento(s)."
            )
        }
    }

    private fun startLockout() {
        viewModelScope.launch {
            for (seconds in lockoutDurationSeconds downTo 1) {
                _uiState.value = LoginUiState.Lockout(seconds)
                delay(1000L)
            }

            failedAttempts = 0
            _uiState.value = LoginUiState.Idle
        }
    }
}