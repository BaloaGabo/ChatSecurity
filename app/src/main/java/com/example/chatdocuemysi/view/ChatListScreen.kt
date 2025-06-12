package com.example.chatdocuemysi.view

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.chatdocuemysi.model.ChatItemModel
import com.example.chatdocuemysi.repository.ChatListRepository
import com.example.chatdocuemysi.viewmodel.ChatListViewModel
import com.example.chatdocuemysi.viewmodel.ChatListViewModelFactory

/**
 * Pantalla que muestra la lista de chats (grupales e individuales) del usuario actual.
 * Permite navegar a chats existentes o iniciar uno nuevo.
 *
 * @param navController El controlador de navegación para manejar las transiciones.
 * @param myUid El UID del usuario actualmente autenticado.
 * @param viewModel El ViewModel que proporciona los datos de la lista de chats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    myUid: String,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(ChatListRepository(myUid))
    )
) {
    // Recolecta la lista de elementos de chat del ViewModel.
    val previews by viewModel.chatItems.collectAsState()
    // Filtra duplicados y ordena los chats por la marca de tiempo del último mensaje.
    val uniquePreviews = remember(previews) {
        previews
            .distinctBy { it.chatId }
            .sortedByDescending { it.timestamp }
    }

    Scaffold(
        // Botón flotante para crear un nuevo chat.
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("newChat") }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
            }
        }
    ) { padding ->
        // Si no hay conversaciones, muestra un mensaje.
        if (uniquePreviews.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes conversaciones iniciadas",
                    style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Muestra la lista de chats si hay conversaciones.
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uniquePreviews) { item: ChatItemModel ->
                    // Determina el ID del "peer" (otro usuario) para chats privados.
                    val peerId = if (item.isGroup) {
                        null // Los chats grupales no tienen un único peer.
                    } else {
                        val parts = item.chatId.split("_")
                        // El peer es el UID que no es el del usuario actual.
                        if (parts[0] == myUid) parts[1] else parts[0]
                    }

                    // Previsualización del último mensaje, truncado si es muy largo.
                    val previewText = item.lastMessage
                        .takeIf { it.length <= 10 } // Si el mensaje es corto, lo muestra completo.
                        ?: (item.lastMessage.take(10) + "…") // Si es largo, lo trunca y añade puntos suspensivos.

                    ListItem(
                        headlineContent = {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text("${item.lastSenderName}: $previewText", // Muestra el nombre del remitente y el mensaje.
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1 // Asegura que el texto no ocupe más de una línea.
                            )
                        },
                        leadingContent = {
                            // Muestra la foto de perfil del chat si está disponible.
                            item.photoUrl.takeIf { it.isNotBlank() }?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.small) // Recorta la imagen con esquinas redondeadas.
                                )
                            }
                        },
                        trailingContent = {
                            Text(
                                text = item.formattedTime, // Muestra la hora formateada del último mensaje.
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Navega al chat correspondiente al hacer clic en el elemento.
                                val route = if (item.isGroup) {
                                    // Para chats grupales, navega a la ruta "groupChat" con el ID y nombre del grupo.
                                    val idEnc = Uri.encode(item.chatId)
                                    val nameEnc = Uri.encode(item.title)
                                    "groupChat/$idEnc/$nameEnc"
                                } else {
                                    // Para chats privados, navega a la ruta "privateChat" con el ID del peer y el nombre.
                                    val peerEnc = Uri.encode(peerId!!)
                                    val nameEnc = Uri.encode(item.title)
                                    "privateChat/$peerEnc/$nameEnc"
                                }
                                navController.navigate(route)
                            }
                    )
                    Divider() // Añade un divisor entre cada elemento de la lista.
                }
            }
        }
    }
}

