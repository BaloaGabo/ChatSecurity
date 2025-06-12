package com.example.chatdocuemysi.view

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.chatdocuemysi.viewmodel.CreateGroupViewModel
import com.example.chatdocuemysi.viewmodel.CreateGroupViewModelFactory
import com.example.chatdocuemysi.viewmodel.UserListViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Pantalla para la creación de un nuevo grupo de chat.
 * Permite al usuario definir el nombre del grupo, seleccionar una foto y añadir miembros.
 *
 * @param navController El controlador de navegación para gestionar las transiciones.
 * @param myUid El UID del usuario actualmente autenticado (el creador del grupo).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    myUid: String
) {
    // ViewModels para obtener datos y manejar la lógica de creación.
    val userListVm: UserListViewModel = viewModel()
    val allUsers by userListVm.users.collectAsState() // Lista de todos los usuarios.

    val vm: CreateGroupViewModel = viewModel(factory = CreateGroupViewModelFactory(myUid))
    val groupName by vm.groupName.collectAsState()       // Nombre del grupo.
    val members by vm.selectedMembers.collectAsState()   // Miembros seleccionados para el grupo.
    val photoUri by vm.photoUri.collectAsState()         // URI de la foto del grupo.
    val creating by vm.creating.collectAsState()         // Estado de creación (true si está creando el grupo).

    // Permiso para leer imágenes (Android 13+ usa READ_MEDIA_IMAGES, versiones anteriores usan READ_EXTERNAL_STORAGE).
    val readPerm = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    )
    // Launcher para abrir el selector de imágenes de la galería.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.onPhotoChange(it) } // Actualiza la URI de la foto en el ViewModel.
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo de texto para el nombre del grupo.
        OutlinedTextField(
            value = groupName,
            onValueChange = vm::onNameChange,
            label = { Text("Nombre de grupo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Caja para seleccionar la foto del grupo.
        Box(
            Modifier
                .size(100.dp)
                .clickable {
                    // Si el permiso está concedido, lanza el selector de imágenes; si no, solicita el permiso.
                    if (readPerm.status.isGranted) launcher.launch("image/*")
                    else readPerm.launchPermissionRequest()
                },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                // Muestra la imagen seleccionada si existe.
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Elegir foto") // Mensaje predeterminado si no hay foto.
            }
        }
        Spacer(Modifier.height(8.dp))

        // Encabezado para la sección de miembros.
        Text("Miembros:", modifier = Modifier.padding(bottom = 4.dp))

        // Lista de usuarios seleccionables para ser miembros del grupo.
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Ocupa el espacio restante disponible.
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
        ) {
            // Filtra la lista de todos los usuarios para excluir al usuario actual.
            items(allUsers.filter { it.uid != myUid }) { user ->
                val checked = user.uid in members // Verifica si el usuario ya está seleccionado.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Checkbox para seleccionar/deseleccionar miembros.
                    Checkbox(
                        checked = checked,
                        onCheckedChange = {
                            val new = if (it) members + user.uid else members - user.uid
                            vm.onMembersChange(new) // Actualiza la lista de miembros seleccionados.
                        }
                    )
                    Text(
                        text = user.nombres,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Botón para crear el grupo.
        Button(
            onClick = {
                vm.create { success, id ->
                    if (success) {
                        // Si el grupo se crea con éxito, navega al chat del grupo.
                        navController.navigate("groupChat/$id/${Uri.encode(groupName)}")
                    } else {
                        // En caso de fallo, se podría mostrar un Snackbar o Toast al usuario.
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // El botón está habilitado si no se está creando y si el nombre y los miembros no están vacíos.
            enabled = !creating && groupName.isNotBlank() && members.isNotEmpty()
        ) {
            Text(if (creating) "Creando…" else "Crear Grupo") // Muestra el texto "Creando..." mientras se procesa.
        }
    }
}