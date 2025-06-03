// src/main/java/com/example/chatdocuemysi/view/ChatScreen.kt
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
import com.example.chatdocuemysi.ui.theme.onPrimaryDark
import com.example.chatdocuemysi.ui.theme.primaryDark
import com.example.chatdocuemysi.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

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
    BackHandler(onBack = onBack)

    val messagesUi by vm.getMessagesUiFlow(chatPath).collectAsState()

    // Para mostrar imagen a pantalla completa
    var fullImageUri by remember { mutableStateOf<String?>(null) }

    // Input
    var messageText by remember { mutableStateOf("") }

    // Scroll state
    val listState = rememberLazyListState()
    var previousCount by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
                    if (chatPath.startsWith("ChatsGrupales/")) {
                        Text(
                            text = receiverName,
                            modifier = Modifier.clickable {
                                val groupId = chatPath.removePrefix("ChatsGrupales/")
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

                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Escribe un mensaje…") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "Enviar imagen")
                }
                TextButton(
                    onClick = {
                        vm.sendText(chatPath, senderId, messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank()
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
                // Dibujamos en el orden recibido: primeros antiguos, luego recientes
                items(messagesUi, key = { it.id }) { msg ->
                    MessageCard(
                        message = msg,
                        isMine = (msg.senderId == senderId),
                        onImageClick = { uri -> fullImageUri = uri }
                    )
                }
            }

            // Auto-scroll al último mensaje (más reciente)
            LaunchedEffect(messagesUi.size) {
                if (messagesUi.size > previousCount) {
                    delay(100)
                    listState.animateScrollToItem(messagesUi.lastIndex)
                }
                previousCount = messagesUi.size
            }

            // Modal para imagen ampliada
            fullImageUri?.let { uri ->
                Dialog(onDismissRequest = { fullImageUri = null }) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .pointerInput(Unit) {
                                detectDragGestures { _, dragAmount ->
                                    if (dragAmount.y > 100f) fullImageUri = null
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
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}
