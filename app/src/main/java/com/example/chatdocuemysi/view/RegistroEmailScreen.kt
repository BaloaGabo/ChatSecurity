package com.example.chatdocuemysi.view

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chatdocuemysi.viewmodel.RegistroEmailViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Pantalla de registro de usuario con correo electrónico.
 * Permite al usuario introducir sus datos (nombre, email, contraseña) y seleccionar una foto de perfil
 * para crear una nueva cuenta.
 *
 * @param navController El [NavController] para gestionar la navegación entre pantallas.
 * @param viewModel El [RegistroEmailViewModel] que maneja la lógica de negocio y el estado de la pantalla.
 * @param onBack La acción a ejecutar cuando se presiona el botón de retroceso del sistema.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RegistroEmailScreen(
    navController: NavController,
    viewModel: RegistroEmailViewModel = viewModel(),
    onBack: () -> Unit
) {
    // Maneja la acción de retroceso del sistema.
    BackHandler(onBack = onBack)

    // Recolectamos los estados del ViewModel para que la UI se actualice automáticamente.
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val repeatPassword by viewModel.repeatPassword.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val context = LocalContext.current // Obtenemos el contexto actual para mostrar Toasts.
    var error by remember { mutableStateOf<String?>(null) } // Estado para mostrar mensajes de error en la UI.

    // Configuración de permisos y del launcher para seleccionar imágenes de la galería.
    val readPerm = rememberPermissionState(
        // Diferenciamos el permiso según la versión de Android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES // Android 13+
        else Manifest.permission.READ_EXTERNAL_STORAGE // Versiones anteriores
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Contrato para obtener contenido (en este caso, imágenes).
    ) { uri: Uri? -> uri?.let { viewModel.actualizarImageUri(it) } } // Si se selecciona una URI, la actualizamos en el ViewModel.

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registro") }, // Título de la barra superior.
                navigationIcon = {
                    // Botón para retroceder en la navegación.
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding) // Aplicamos el padding proporcionado por el Scaffold.
                .padding(16.dp), // Padding adicional para el contenido de la columna.
            horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos horizontalmente.
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio vertical entre los elementos.
        ) {
            // Sección para la foto de perfil.
            Box(modifier = Modifier.size(100.dp)) {
                if (imageUri != null) {
                    // Si hay una URI de imagen seleccionada, la mostramos.
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape), // Recortamos la imagen en forma de círculo.
                        contentScale = ContentScale.Crop // Escala la imagen para rellenar el espacio.
                    )
                } else {
                    // Si no hay imagen seleccionada, mostramos un icono de cámara y lo hacemos clickeable.
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable {
                                // Al hacer clic, verificamos los permisos y lanzamos el selector de imágenes.
                                if (readPerm.status.isGranted) launcher.launch("image/*")
                                else readPerm.launchPermissionRequest()
                            }
                    )
                }
            }

            // Campo de texto para el nombre completo.
            OutlinedTextField(
                value = displayName,
                onValueChange = viewModel::actualizarDisplayName,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            // Campo de texto para el email.
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::actualizarEmail,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            // Campo de texto para la contraseña (oculta el texto).
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::actualizarPassword,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            // Campo de texto para repetir la contraseña (oculta el texto).
            OutlinedTextField(
                value = repeatPassword,
                onValueChange = viewModel::actualizarRepeatPassword,
                label = { Text("Repetir contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            // Si hay un error, lo mostramos con un color de error.
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // Botón para registrar al usuario.
            Button(
                onClick = {
                    // Verificamos que las contraseñas coincidan antes de proceder.
                    if (password != repeatPassword) {
                        error = "Las contraseñas no coinciden"
                        return@Button
                    }
                    error = null // Limpiamos errores anteriores si las contraseñas coinciden.
                    viewModel.registrarUsuario { ok, msg ->
                        if (ok) {
                            // Si el registro es exitoso, mostramos un Toast y navegamos a la lista de chats.
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            navController.navigate("chatList") {
                                // Limpiamos el back stack para que el usuario no pueda volver a la pantalla de login/registro.
                                popUpTo("opcionesLogin") { inclusive = true }
                            }
                        } else {
                            // Si el registro falla, mostramos el mensaje de error.
                            error = msg
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // El botón solo está habilitado si todos los campos requeridos no están vacíos.
                enabled = displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Registrarse") // Texto del botón.
            }
        }
    }
}
