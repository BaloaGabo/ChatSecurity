package com.example.chatdocuemysi.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatdocuemysi.viewmodel.GroupDetailViewModel
import com.example.chatdocuemysi.viewmodel.GroupDetailViewModelFactory
import com.example.chatdocuemysi.viewmodel.UserListViewModel

/**
 * Pantalla para administrar los miembros de un grupo de chat.
 * Permite a los administradores del grupo añadir, remover, promover o despromover miembros.
 *
 * @param groupId El ID del grupo a administrar.
 * @param myUid El UID del usuario actual.
 * @param onBack La acción a realizar al presionar el botón de retroceso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGroupScreen(
    groupId: String,
    myUid: String,
    onBack: () -> Unit
) {
    // ViewModel para obtener la lista de todos los usuarios.
    val userListVm: UserListViewModel = viewModel()
    val allUsers by userListVm.users.collectAsState()

    // ViewModel para los detalles específicos del grupo y las acciones de administración.
    val vm: GroupDetailViewModel = viewModel(
        factory = GroupDetailViewModelFactory(groupId, myUid)
    )
    val detail by vm.detail.collectAsState() // Detalles del grupo (nombre, miembros, admins).
    val isAdmin by vm.isAdmin.collectAsState() // Indica si el usuario actual es administrador del grupo.

    Scaffold(
        topBar = {
            // Barra superior de la pantalla.
            CenterAlignedTopAppBar(
                navigationIcon = {
                    // Botón de retroceso.
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text(detail.groupName) } // Título de la barra con el nombre del grupo.
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text("Miembros", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            // Lista de miembros del grupo.
            LazyColumn(Modifier.weight(1f)) {
                items(detail.members) { uid ->
                    val user = allUsers.find { it.uid == uid } // Encuentra los detalles del usuario por UID.
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(user?.nombres ?: uid) // Muestra el nombre del miembro o su UID.
                        if (isAdmin) {
                            // Opciones de administración solo si el usuario actual es admin.
                            Row {
                                val isThisAdmin = uid in detail.admins // Verifica si el miembro actual es admin.
                                Text(
                                    text = if (isThisAdmin) "🔰 Admin" else "⭐ Hacer admin", // Texto para promover/despromover.
                                    modifier = Modifier
                                        .clickable {
                                            // Acción para promover o despromover un miembro.
                                            if (isThisAdmin) vm.demote(uid) else vm.promote(uid)
                                        }
                                        .padding(end = 16.dp)
                                )
                                Text(
                                    text = "❌", // Icono para remover miembro.
                                    modifier = Modifier.clickable { vm.removeMember(uid) } // Acción para remover un miembro.
                                )
                            }
                        }
                    }
                }
            }

            // Sección para añadir nuevos miembros, solo visible para administradores.
            if (isAdmin) {
                Divider()
                Text("Añadir nuevo miembro", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                // Lista de usuarios que no son miembros actuales del grupo.
                LazyColumn(Modifier.weight(1f)) {
                    items(allUsers.filter { it.uid !in detail.members }) { user ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.addMember(user.uid) } // Acción para añadir un miembro.
                                .padding(8.dp)
                        ) {
                            Text(user.nombres) // Muestra el nombre del usuario disponible para añadir.
                        }
                    }
                }
            }
        }
    }
}
