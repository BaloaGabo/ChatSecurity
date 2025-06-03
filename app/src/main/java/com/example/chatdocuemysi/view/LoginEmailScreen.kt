// src/main/java/com/example/chatdocuemysi/view/LoginEmailScreen.kt
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginEmailScreen(
    navController: NavController,
    viewModel: LoginEmailViewModel = viewModel()
) {
    val email    by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope            = rememberCoroutineScope()

    // Estado para alternar visibilidad de la contraseña
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
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::actualizarEmail,
                label = { Text(stringResource(R.string.et_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = viewModel::actualizarPassword,
                label = { Text(stringResource(R.string.et_password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton (onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                    }
                }
            )

            Button(
                onClick = {
                    viewModel.iniciarSesion { success, errorMessage ->
                        if (success) {
                            navController.navigate("chatList") {
                                popUpTo("opcionesLogin") { inclusive = true }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(errorMessage ?: "Error desconocido")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_ingresar))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("registroEmail") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_registrar))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("olvidePassword") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.olvide_contrasena))
            }
        }
    }
}




