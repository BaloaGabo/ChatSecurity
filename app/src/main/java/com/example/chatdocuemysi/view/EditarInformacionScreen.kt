package com.example.chatdocuemysi.view

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chatdocuemysi.R
import com.example.chatdocuemysi.viewmodel.EditarInformacionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Pantalla para editar la información del perfil del usuario, como el nombre y la foto de perfil.
 *
 * @param navController El [NavController] para manejar la navegación.
 * @param viewModel El [EditarInformacionViewModel] que proporciona y gestiona los datos de la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditarInformacionScreen(
    navController: NavController,
    viewModel: EditarInformacionViewModel
) {
    // Recolecta los estados del ViewModel.
    val nombres by viewModel.nombres.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val context = LocalContext.current
    var newImageUri by remember { mutableStateOf<Uri?>(null) } // Estado para la URI de la nueva imagen seleccionada.

    // Launcher para seleccionar una imagen de la galería.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            newImageUri = uri // Asigna la URI seleccionada al estado.
        }
    )

    // Efecto que se lanza cuando `newImageUri` cambia.
    LaunchedEffect(newImageUri) {
        newImageUri?.let { uri ->
            viewModel.actualizarImagen(uri) { success, errorMessage ->
                if (!success) {
                    // Muestra un Toast si falla la actualización de la imagen.
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Estado del permiso para leer imágenes (diferente para Android 13+ y versiones anteriores).
    val readMediaImagesPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.Txt_titulo_edit)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente.
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                // Muestra la imagen de perfil actual.
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.profile_image),
                    placeholder = painterResource(id = R.drawable.ic_perfil), // Imagen por defecto.
                    error = painterResource(id = R.drawable.ic_error), // Imagen en caso de error de carga.
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )
                // Botón superpuesto para editar la imagen de perfil.
                EditImageButton(launcher = launcher, readMediaImagesPermissionState = readMediaImagesPermissionState)
            }
            // Campo de texto para editar el nombre del usuario.
            OutlinedTextField(
                value = nombres,
                onValueChange = { viewModel.actualizarNombres(it) }, // Llama al ViewModel para actualizar el nombre.
                label = { Text(stringResource(R.string.et_nombres)) },
                modifier = Modifier.fillMaxWidth()
            )
            // Botón para guardar los cambios.
            Button(
                onClick = {
                    viewModel.actualizarInfo { success, errorMessage ->
                        if (!success) {
                            // Muestra un Toast si falla la actualización.
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            // Muestra un Toast de éxito y regresa a la pantalla anterior.
                            Toast.makeText(context, "Información actualizada", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_Actualizar))
            }
        }
    }
}

/**
 * Composable para el botón de edición de imagen, que maneja la lógica de permisos.
 *
 * @param launcher El ActivityResultLauncher para seleccionar imágenes.
 * @param readMediaImagesPermissionState El [PermissionState] para el permiso de lectura de imágenes.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditImageButton(
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    readMediaImagesPermissionState: PermissionState
) {
    Image(
        painter = painterResource(id = R.drawable.icono_editar),
        contentDescription = "Edit Image",
        modifier = Modifier
            .clickable {
                // Lógica de permisos para abrir el selector de imágenes.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (readMediaImagesPermissionState.status.isGranted) {
                        launcher.launch("image/*") // Lanza el selector si el permiso está concedido.
                    } else {
                        readMediaImagesPermissionState.launchPermissionRequest() // Solicita el permiso.
                    }
                } else {
                    // Para versiones anteriores, simplemente lanza el selector.
                    launcher.launch("image/*")
                }
            }
    )
}