package com.example.chatdocuemysi.view

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.chatdocuemysi.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de chat que muestra los mensajes y permite enviar nuevos mensajes de texto e imagen.
 *
 * @param navController El controlador de navegación para manejar la navegación.
 * @param chatPath La ruta del chat en la base de datos (e.g., "MensajesIndividuales/uid1/uid2" o "ChatsGrupales/groupId").
 * @param senderId El UID del usuario actual (remitente).
 * @param receiverName El nombre del receptor (otro usuario o nombre del grupo).
 * @param onBack La acción a ejecutar cuando se presiona el botón de retroceso.
 * @param vm El ViewModel que maneja la lógica del chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatPath: String,
    senderId: String,
    receiverName: String,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    // Maneja el botón de retroceso del sistema.
    BackHandler(onBack = onBack)

    // Recolecta el flujo de mensajes de la UI desde el ViewModel.
    val messagesUi by vm.getMessagesUiFlow(chatPath).collectAsState()

    // Estado para controlar la visibilidad y el contenido de la imagen a pantalla completa.
    var fullImageUri by remember { mutableStateOf<String?>(null) }

    // Estado para el texto del mensaje de entrada.
    var messageText by remember { mutableStateOf("") }

    // Estado para el scroll de la lista de mensajes.
    val listState = rememberLazyListState()
    var previousCount by remember { mutableIntStateOf(0) }

    // Launcher para seleccionar imágenes de la galería.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Si se selecciona una URI, la envía como mensaje de imagen.
        uri?.let { vm.sendImage(chatPath, senderId, it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = {
                    // Si es un chat grupal, el título es clickable para ir a la pantalla de administración del grupo.
                    if (chatPath.startsWith("ChatsGrupales/")) {
                        Text(
                            text = receiverName,
                            modifier = Modifier.clickable {
                                val groupId = chatPath.removePrefix("ChatsGrupales/")
                                // Navega a la pantalla de administración del grupo.
                                navController.navigate("adminGroup/$groupId/${Uri.encode(receiverName)}")
                            }
                        )
                    } else {
                        Text(receiverName)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors()
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Campo de texto para escribir mensajes.
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Escribe un mensaje…") },
                    modifier = Modifier.weight(1f)
                )

                // Botón para seleccionar y enviar una imagen.
                IconButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "Enviar imagen")
                }

                // Botón para enviar el mensaje de texto.
                TextButton(
                    onClick = {
                        vm.sendText(chatPath, senderId, messageText) // Envía el mensaje de texto.
                        messageText = "" // Limpia el campo de texto.
                    },
                    enabled = messageText.isNotBlank() // Habilita el botón solo si el texto no está vacío.
                ) {
                    Text("Enviar")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Dibuja los mensajes en el orden en que fueron recibidos (del más antiguo al más reciente).
                items(messagesUi, key = { it.id }) { msg ->
                    MessageCard(
                        message = msg,
                        isMine = (msg.senderId == senderId),  // Determina si el mensaje fue enviado por el usuario actual.
                        onImageClick = { uri -> fullImageUri = uri } // Callback para mostrar la imagen a pantalla completa.
                    )
                }
            }

            // Efecto lanzado para hacer auto-scroll al último mensaje cuando se añade uno nuevo.
            LaunchedEffect(messagesUi.size) {
                if (messagesUi.size > previousCount) {
                    delay(100) // Pequeño retraso para permitir que la UI se actualice antes del scroll.
                    listState.animateScrollToItem(messagesUi.lastIndex) // Desplaza al último mensaje.
                }
                previousCount = messagesUi.size // Actualiza el contador de mensajes.
            }

            // Diálogo modal para mostrar la imagen ampliada.
            fullImageUri?.let { uri ->
                Dialog(onDismissRequest = { fullImageUri = null }) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)) // Fondo semi-transparente.
                            .pointerInput(Unit) {
                                // Detecta gestos de arrastre para cerrar la imagen.
                                detectDragGestures { _, dragAmount ->
                                    if (dragAmount.y > 100f) fullImageUri = null // Cierra si se arrastra hacia abajo.
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight(0.8f)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri), // Carga la imagen de forma asíncrona.
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit // Ajusta la imagen para que quepa.
                            )
                        }
                    }
                }
            }
        }
    }
}
