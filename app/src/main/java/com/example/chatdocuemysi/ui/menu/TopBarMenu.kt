package com.example.chatdocuemysi.ui.menu

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth

/**
 * Composable que define la barra superior de la aplicación.
 * Muestra diferentes opciones según el estado de autenticación del usuario.
 *
 * @param navController El controlador de navegación para manejar las acciones de navegación.
 * @param isUserAuthenticated Booleano que indica si el usuario está autenticado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarMenu(
    navController: NavHostController,
    isUserAuthenticated: Boolean
) {
    // Si el usuario NO está autenticado, solo muestra una barra superior con el título de la app.
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

    // A partir de aquí, el usuario está autenticado.
    val firebaseAuth = FirebaseAuth.getInstance()
    // Estado para controlar la expansión del menú desplegable.
    var expanded by remember { mutableStateOf(false) }

    // Obtiene la ruta actual para decidir si ocultar el menú de opciones.
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    // Oculta el menú de tres puntos (⋮) en las pantallas de chat individuales o grupales.
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
    // Barra superior con el menú de opciones para usuarios autenticados (cuando no están en un chat).
    CenterAlignedTopAppBar(
        title = { Text("Chat DocuEmysi") },
        actions = {
            // Icono de tres puntos para abrir el menú.
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Abrir menú")
            }
            // Menú desplegable.
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Opción para navegar a la pantalla de edición de información.
                DropdownMenuItem(
                    text = { Text("Editar Información") },
                    onClick = {
                        expanded = false
                        navController.navigate("editarInformacion")
                    }
                )
                // Opción para navegar a la lista de chats.
                DropdownMenuItem(
                    text = { Text("Lista de Chats") },
                    onClick = {
                        expanded = false
                        navController.navigate("chatList")
                    }
                )
                // Opción para cerrar sesión.
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        expanded = false
                        firebaseAuth.signOut() // Cierra la sesión de Firebase.
                        // Muestra un Toast de confirmación.
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
