package com.techsolutions.worqee.views.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techsolutions.worqee.utils.RegisterValidator
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

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var birthdayError by remember { mutableStateOf<String?>(null) }

    var globalError by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess()

            is LoginUiState.Error -> {
                globalError = (uiState as LoginUiState.Error).message
            }

            else -> {
                if (uiState !is LoginUiState.Error) {
                    globalError = null
                }
            }
        }
    }

    val isLocked = uiState is LoginUiState.Lockout
    val isLoading = uiState is LoginUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                    containerColor = MaterialTheme.colorScheme.errorContainer
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
                        text = "Demasiados intentos fallidos",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Text(
                        text = "Intenta de nuevo en $remainingSeconds segundo${if (remainingSeconds != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        globalError?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isRegisterMode) {
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = null
                    globalError = null
                },
                label = { Text("Nombre de usuario") },
                isError = usernameError != null,
                supportingText = usernameError?.let { message ->
                    {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLocked
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthday,
                onValueChange = {},
                label = { Text("Fecha de nacimiento") },
                isError = birthdayError != null,
                supportingText = birthdayError?.let { message ->
                    {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                enabled = !isLocked,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (!isLocked) {
                                val maxCalendar = Calendar.getInstance().apply {
                                    add(Calendar.YEAR, -RegisterValidator.MINIMUM_AGE)
                                }

                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        birthday =
                                            "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                                        birthdayError = null
                                        globalError = null
                                    },
                                    maxCalendar.get(Calendar.YEAR),
                                    maxCalendar.get(Calendar.MONTH),
                                    maxCalendar.get(Calendar.DAY_OF_MONTH)
                                ).apply {
                                    datePicker.maxDate = maxCalendar.timeInMillis
                                }.show()
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
            onValueChange = {
                email = it
                emailError = null
                globalError = null
            },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = emailError?.let { message ->
                {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
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
                globalError = null
            },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { message ->
                {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLocked,
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
                    globalError = null
                },
                label = { Text("Confirmar contraseña") },
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { message ->
                    {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLocked,
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
                globalError = null

                if (isRegisterMode) {
                    val errors = RegisterValidator.validate(
                        username = username,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        birthday = birthday
                    )

                    usernameError = errors.usernameError
                    emailError = errors.emailError
                    passwordError = errors.passwordError
                    confirmPasswordError = errors.confirmPasswordError
                    birthdayError = errors.birthdayError

                    if (!errors.hasErrors) {
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

                usernameError = null
                emailError = null
                passwordError = null
                confirmPasswordError = null
                birthdayError = null
                globalError = null

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