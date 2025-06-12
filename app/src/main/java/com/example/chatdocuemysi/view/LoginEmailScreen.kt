package com.example.chatdocuemysi.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatdocuemysi.R
import com.example.chatdocuemysi.viewmodel.LoginEmailViewModel
import kotlinx.coroutines.launch

/**
 * ## Pantalla de Inicio de Sesión con Correo Electrónico
 *
 * Esta función Composable proporciona la interfaz de usuario para que los usuarios inicien sesión
 * con su correo electrónico y contraseña. Incluye campos para el correo electrónico y la contraseña,
 * opciones para alternar la visibilidad de la contraseña y botones para iniciar sesión, registrarse
 * y recuperar la contraseña.
 *
 * @param navController El [NavController] utilizado para navegar entre pantallas.
 * @param viewModel El [LoginEmailViewModel] que maneja la lógica y el estado del inicio de sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginEmailScreen(
    navController: NavController,
    viewModel: LoginEmailViewModel = viewModel()
) {
    // Recolecta los estados del ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    // Estado para gestionar los mensajes de SnackBar
    val snackbarHostState = remember { SnackbarHostState() }
    // Ámbito de corrutina para lanzar funciones suspendidas (ej. mostrar SnackBar)
    val scope = rememberCoroutineScope()

    // Estado para alternar la visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.txt_login)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campo de entrada para el correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::actualizarEmail,
                label = { Text(stringResource(R.string.et_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de entrada para la contraseña con alternador de visibilidad
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::actualizarPassword,
                label = { Text(stringResource(R.string.et_password)) },
                modifier = Modifier.fillMaxWidth(),
                // Aplica transformación visual según el estado `passwordVisible`
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    // Icono para alternar la visibilidad de la contraseña
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton (onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                    }
                }
            )

            // Botón de inicio de sesión
            Button(
                onClick = {
                    viewModel.iniciarSesion { success, errorMessage ->
                        if (success) {
                            // Si el inicio de sesión es exitoso, navega a la lista de chats y limpia el back stack
                            navController.navigate("chatList") {
                                popUpTo("opcionesLogin") { inclusive = true }
                            }
                        } else {
                            // Si el inicio de sesión falla, muestra un mensaje de error usando SnackBar
                            scope.launch {
                                snackbarHostState.showSnackbar(errorMessage ?: "Error desconocido")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Habilita el botón solo si los campos de correo y contraseña no están vacíos
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_ingresar))
            }

            Spacer(Modifier.height(8.dp))

            // Botón de registro
            Button(
                onClick = { navController.navigate("registroEmail") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_registrar))
            }

            Spacer(Modifier.height(8.dp))

            // Botón de "Olvidé mi contraseña"
            Button(
                onClick = { navController.navigate("olvidePassword") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.olvide_contrasena))
            }
        }
    }
}