package com.example.chatdocuemysi.view

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.chatdocuemysi.model.User
import com.example.chatdocuemysi.viewmodel.UserListViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla que muestra una lista de contactos (usuarios) con quienes el usuario actual puede iniciar chats.
 * Permite iniciar un chat privado o navegar a la pantalla de creación de grupos.
 *
 * @param navController El controlador de navegación para gestionar las transiciones entre pantallas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    navController: NavController
) {
    // Obtiene la instancia del ViewModel para la lista de usuarios.
    val viewModel: UserListViewModel = viewModel()
    // Recolecta el estado de la lista de usuarios del ViewModel.
    val users by viewModel.users.collectAsState()
    // Obtiene el UID del usuario actual. Si no está autenticado, la función retorna temprano.
    val myUid = FirebaseAuth.getInstance().uid ?: return

    Scaffold(
        floatingActionButton = {
            // Botón flotante para crear un nuevo grupo.
            FloatingActionButton(onClick = { navController.navigate("createGroup") }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Grupo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre cada elemento de la lista.
        ) {
            // Itera sobre la lista de usuarios, filtrando al usuario actual para no listarse a sí mismo.
            items(users.filter { it.uid != myUid }) { user: User ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Al hacer clic en un usuario, navega a la pantalla de chat privado con ese usuario.
                            val uidEnc = Uri.encode(user.uid) // Codifica el UID para pasarlo como argumento de navegación.
                            val nameEnc = Uri.encode(user.nombres) // Codifica el nombre para pasarlo como argumento.
                            navController.navigate("privateChat/$uidEnc/$nameEnc")
                        },
                    elevation = CardDefaults.cardElevation(4.dp) // Añade una ligera sombra a la tarjeta.
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Muestra el nombre del usuario.
                        Text(
                            text = user.nombres,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f) // Ocupa el espacio disponible.
                        )
                        // Muestra la imagen de perfil del usuario.
                        Image(
                            painter = rememberAsyncImagePainter(user.imagen), // Carga la imagen de forma asíncrona.
                            contentDescription = "Foto de ${user.nombres}",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape), // Recorta la imagen en forma de círculo.
                            contentScale = ContentScale.Crop // Escala la imagen para llenar el espacio.
                        )
                    }
                }
            }
        }
    }
}



