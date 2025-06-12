package com.example.chatdocuemysi.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Pantalla para la recuperación de contraseña. Permite al usuario introducir su correo electrónico
 * para recibir un enlace de restablecimiento de contraseña.
 *
 * @param navController El controlador de navegación para manejar las transiciones.
 * @param onSent Callback que se ejecuta cuando el enlace de recuperación se envía exitosamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    onSent: () -> Unit
) {
    var email by remember { mutableStateOf("") } // Estado para el campo de texto del correo electrónico.

    // Host para mostrar mensajes temporales (Snackbars) al usuario.
    val snackbarHostState = remember { SnackbarHostState() }
    // Un CoroutineScope para lanzar corrutinas de manera segura dentro de la composición.
    val scope = rememberCoroutineScope()
    // Instancia de FirebaseAuth para interactuar con la autenticación de Firebase.
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    // Botón para retroceder a la pantalla anterior.
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Asigna el SnackbarHost al Scaffold.
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica el padding del Scaffold.
                .padding(16.dp), // Padding adicional para el contenido de la columna.
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre los elementos de la columna.
        ) {
            // Campo de texto para que el usuario introduzca su correo electrónico.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            // Botón para enviar el enlace de recuperación de contraseña.
            Button(
                onClick = {
                    // Envía el correo de restablecimiento de contraseña utilizando Firebase Auth.
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            onSent() // Llama al callback si el envío es exitoso.
                        }
                        .addOnFailureListener { e ->
                            // En caso de fallo, muestra un Snackbar con el mensaje de error.
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Error: ${e.message}"
                                )
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar enlace de recuperación")
            }
        }
    }
}

