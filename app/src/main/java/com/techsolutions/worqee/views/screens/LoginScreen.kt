package com.techsolutions.worqee.views.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techsolutions.worqee.viewModel.LoginUiState
import com.techsolutions.worqee.viewModel.LoginViewModel
import java.util.Calendar

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isRegisterMode by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess()

            is LoginUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as LoginUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.resetState()
            }

            else -> Unit
        }
    }

    val isLocked = uiState is LoginUiState.Lockout
    val isLoading = uiState is LoginUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Worqee",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isRegisterMode) {
                "Crea tu cuenta"
            } else {
                "Inicia sesión para continuar"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLocked) {
            val remainingSeconds = (uiState as LoginUiState.Lockout).remainingSeconds

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔒 Demasiados intentos fallidos",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Red
                    )

                    Text(
                        text = "Intenta de nuevo en $remainingSeconds segundo${if (remainingSeconds != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isRegisterMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLocked
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthday,
                onValueChange = {},
                label = { Text("Fecha de nacimiento") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                enabled = !isLocked,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (!isLocked) {
                                val calendar = Calendar.getInstance()

                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        birthday =
                                            "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        }
                    ) {
                        Text("📅")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLocked
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLocked,
            isError = passwordError != null,
            supportingText = passwordError?.let {
                {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        }
                    )
                }
            }
        )

        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text("Confirmar contraseña") },
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLocked,
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let {
                    {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (confirmPasswordVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isRegisterMode) {
                    val hasSpecialCharacter = password.any { !it.isLetterOrDigit() }
                    var hasError = false

                    if (password.length < 8 || !hasSpecialCharacter) {
                        passwordError = "Mínimo 8 caracteres y un carácter especial"
                        hasError = true
                    }

                    if (password != confirmPassword) {
                        confirmPasswordError = "Las contraseñas no coinciden"
                        hasError = true
                    }

                    if (!hasError) {
                        viewModel.register(
                            email = email,
                            password = password,
                            username = username,
                            birthday = birthday
                        )
                    }
                } else {
                    viewModel.login(
                        email = email,
                        password = password
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isLocked
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )

                isLocked -> {
                    val remainingSeconds = (uiState as LoginUiState.Lockout).remainingSeconds
                    Text("Bloqueado ($remainingSeconds s)")
                }

                else -> Text(
                    if (isRegisterMode) {
                        "Registrarse"
                    } else {
                        "Iniciar sesión"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode

                email = ""
                password = ""
                confirmPassword = ""
                username = ""
                birthday = ""

                passwordError = null
                confirmPasswordError = null

                passwordVisible = false
                confirmPasswordVisible = false

                viewModel.resetState()
            },
            enabled = !isLocked
        ) {
            Text(
                if (isRegisterMode) {
                    "¿Ya tienes cuenta? Inicia sesión"
                } else {
                    "¿No tienes cuenta? Regístrate"
                }
            )
        }
    }
}