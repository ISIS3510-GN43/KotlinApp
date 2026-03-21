package com.techsolutions.worqee.ui.screens.login

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techsolutions.worqee.ui.screens.login.LoginViewModel
import java.util.Calendar

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current

    var gmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var cumpleanios by remember { mutableStateOf("") }

    var esModoRegistro by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Solo manejamos Success y Error con efectos secundarios
    // Lockout se maneja inline en la UI
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess()
            is LoginUiState.Error -> {
                Toast.makeText(context, (uiState as LoginUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
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
        Text(text = "Worqee", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (esModoRegistro) "Crea tu cuenta" else "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Banner de lockout — visible solo cuando la cuenta está bloqueada temporalmente
        if (isLocked) {
            val segundos = (uiState as LoginUiState.Lockout).segundosRestantes
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
                        text = "Intenta de nuevo en $segundos segundo${if (segundos != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (esModoRegistro) {
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
                value = cumpleanios,
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
                                val calendario = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, anio, mes, dia ->
                                        cumpleanios = "$anio-${(mes + 1).toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}"
                                    },
                                    calendario.get(Calendar.YEAR),
                                    calendario.get(Calendar.MONTH),
                                    calendario.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        }
                    ) { Text("📅") }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = gmail,
            onValueChange = { gmail = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLocked
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLocked
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (esModoRegistro) viewModel.register(gmail, password, username, cumpleanios, context)
                else viewModel.login(gmail, password, context)
            },
            modifier = Modifier.fillMaxWidth(),
            // Deshabilitado durante loading Y durante lockout
            enabled = !isLoading && !isLocked
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                isLocked -> {
                    val segundos = (uiState as LoginUiState.Lockout).segundosRestantes
                    Text("Bloqueado ($segundos s)")
                }
                else -> Text(if (esModoRegistro) "Registrarse" else "Iniciar sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                esModoRegistro = !esModoRegistro
                gmail = ""; password = ""; username = ""; cumpleanios = ""
                viewModel.resetState()
            },
            enabled = !isLocked
        ) {
            Text(if (esModoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }
    }
}