package com.example.chatdocuemysi.view

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateGroupScreen(navController: NavController, myUid: String) {
    val userListVm: UserListViewModel = viewModel()
    val users by userListVm.users.collectAsState()
    val vm: CreateGroupViewModel = viewModel(factory = CreateGroupViewModelFactory(myUid))
    val groupName by vm.groupName.collectAsState()
    val members by vm.selectedMembers.collectAsState()
    val photoUri by vm.photoUri.collectAsState()
    val creating by vm.creating.collectAsState()

    // image picker
    val readPerm = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { vm.onPhotoChange(it) }
        }

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier
                .size(100.dp)
                .clickable {
                    if (readPerm.status.isGranted) launcher.launch("image/*")
                    else readPerm.launchPermissionRequest()
                }, contentAlignment = Alignment.Center) {
                if (photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                } else Text("Elegir foto")
            }
            OutlinedTextField(
                value = groupName,
                onValueChange = vm::onNameChange,
                label = { Text("Nombre de grupo") },
                modifier = Modifier.fillMaxWidth(1f)
            )

        }
        Spacer(Modifier.height(8.dp))
        Text("Miembros:")
        LazyColumn(Modifier.height(200.dp)) {
            items(users.filter { it.uid != myUid }) { user ->
                val checked = user.uid in members
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked, onCheckedChange = {
                        val new = if (it) members + user.uid else members - user.uid
                        vm.onMembersChange(new)
                    })
                    Text(user.nombres)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                vm.create { ok, id ->
                    if (ok) {
                        navController.navigate("groupChat/$id/${Uri.encode(groupName)}")
                    } else {
                        // mostrar error
                    }
                }
            },
            enabled = !creating && groupName.isNotBlank() && members.isNotEmpty()
        ) {
            Text(if (creating) "Creando…" else "Crear Grupo")
        }
    }
}
