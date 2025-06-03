// src/main/java/com/example/chatdocuemysi/view/MessageCard.kt
package com.example.chatdocuemysi.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.chatdocuemysi.model.MessageUiModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageCard(
    message: MessageUiModel,
    isMine: Boolean,
    onImageClick: (String) -> Unit
) {
    val dateTimeFormat = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    val dateTime = dateTimeFormat.format(Date(message.timestamp))

    Column(
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelSmall
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                when {
                    // 1) Imagen normal
                    message.type == "image" && !message.imageUrl.isNullOrBlank() -> {
                        Image(
                            painter = rememberAsyncImagePainter(message.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(180.dp)
                                .clickable { onImageClick(message.imageUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    // 2) Placeholder de imagen expirada
                    message.type == "text" && message.text == "Imagen expirada" -> {
                        Text(
                            text = "🖼️ Imagen expirada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    // 3) Texto normal
                    message.text.isNotBlank() -> {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(top = 4.dp),
                            color = if (isMine)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Text(
            text = dateTime,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

