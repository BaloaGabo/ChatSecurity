// src/main/java/com/example/chatdocuemysi/view/ChatListScreen.kt
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    myUid: String,
    viewModel: ChatListViewModel = viewModel(
        factory = ChatListViewModelFactory(ChatListRepository(myUid))
    )
) {
    val previews by viewModel.chatItems.collectAsState()
    val uniquePreviews = remember(previews) {
        previews
            .distinctBy { it.chatId }
            .sortedByDescending { it.timestamp }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("newChat") }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
            }
        }
    ) { padding ->
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
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uniquePreviews) { item: ChatItemModel ->
                    // Si es chat privado, extraemos el peer real:
                    val peerId = if (item.isGroup) {
                        null
                    } else {
                        val parts = item.chatId.split("_")
                        if (parts[0] == myUid) parts[1] else parts[0]
                    }

                    val previewText = item.lastMessage
                        .takeIf { it.length <= 10 }
                        ?: (item.lastMessage.take(10) + "…")

                    ListItem(
                        headlineContent = {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                        },
                        supportingContent = {
                            Text("${item.lastSenderName}: $previewText",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1)
                        },
                        leadingContent = {
                            item.photoUrl.takeIf { it.isNotBlank() }?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                            }
                        },
                        trailingContent = {
                            Text(
                                text = item.formattedTime,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val route = if (item.isGroup) {
                                    val idEnc = Uri.encode(item.chatId)
                                    val nameEnc = Uri.encode(item.title)
                                    "groupChat/$idEnc/$nameEnc"
                                } else {
                                    // aquí usamos el peerId real
                                    val peerEnc = Uri.encode(peerId!!)
                                    val nameEnc = Uri.encode(item.title)
                                    "privateChat/$peerEnc/$nameEnc"
                                }
                                navController.navigate(route)
                            }
                    )
                    Divider()
                }
            }
        }
    }
}

