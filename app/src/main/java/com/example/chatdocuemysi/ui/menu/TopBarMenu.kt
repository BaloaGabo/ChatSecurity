// ui/menu/TopBarMenu.kt
package com.example.chatdocuemysi.ui.menu

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarMenu(
    navController: NavHostController,
    isUserAuthenticated: Boolean
) {
    // si NO está autenticado, solo pintamos la app-bar con el título
    if (!isUserAuthenticated) {
        CenterAlignedTopAppBar(
            title = { Text("Chat DocuEmysi") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor         = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        return
    }

    // a partir de aquí sabemos que sí está autenticado
    val firebaseAuth = FirebaseAuth.getInstance()
    var expanded by remember { mutableStateOf(false) }

    // ocultar el menú de ⋮ en las pantallas de chat
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    if (route?.startsWith("privateChat/") == true || route?.startsWith("groupChat/") == true) {
        CenterAlignedTopAppBar(
            title = { Text("Chat DocuEmysi") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor         = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        return
    }

    CenterAlignedTopAppBar(
        title = { Text("Chat DocuEmysi") },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Abrir menú")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar Información") },
                    onClick = {
                        expanded = false
                        navController.navigate("editarInformacion")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lista de Chats") },
                    onClick = {
                        expanded = false
                        navController.navigate("chatList")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        expanded = false
                        firebaseAuth.signOut()
                        Toast.makeText(navController.context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor         = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor      = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
