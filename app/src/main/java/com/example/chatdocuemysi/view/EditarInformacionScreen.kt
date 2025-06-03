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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditarInformacionScreen(navController: NavController,
                            viewModel: EditarInformacionViewModel) {
    val nombres by viewModel.nombres.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val context = LocalContext.current
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            newImageUri = uri
        }
    )

    LaunchedEffect(newImageUri) {
        newImageUri?.let { uri ->
            viewModel.actualizarImagen(uri) { success, errorMessage ->
                if (!success) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val readMediaImagesPermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE // For older versions
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.profile_image),
                    placeholder = painterResource(id = R.drawable.ic_perfil),
                    error = painterResource(id = R.drawable.ic_error),
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )
                EditImageButton(launcher = launcher, readMediaImagesPermissionState = readMediaImagesPermissionState)
            }
            OutlinedTextField(
                value = nombres,
                onValueChange = { viewModel.actualizarNombres(it) },
                label = { Text(stringResource(R.string.et_nombres)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.actualizarInfo { success, errorMessage ->
                    if (!success) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Información actualizada", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_Actualizar))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditImageButton(launcher: androidx.activity.result.ActivityResultLauncher<String>, readMediaImagesPermissionState: PermissionState) {
    Image(
        painter = painterResource(id = R.drawable.icono_editar),
        contentDescription = "Edit Image",
        modifier = Modifier
            .clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (readMediaImagesPermissionState.status.isGranted) {
                        launcher.launch("image/*")
                    } else {
                        readMediaImagesPermissionState.launchPermissionRequest()
                    }
                } else {
                    // For older versions, READ_EXTERNAL_STORAGE might be needed
                    // You might need to handle this differently based on your minSdk
                    launcher.launch("image/*")
                }
            }
    )
}